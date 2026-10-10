package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.TotpLoginChallengeCache;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.LoginDetails;
import com.buguagaoshu.tiktube.dto.PasswordDto;
import com.buguagaoshu.tiktube.entity.InvitationCodeEntity;
import com.buguagaoshu.tiktube.entity.UserRoleEntity;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.exception.NeedToCheckEmailException;
import com.buguagaoshu.tiktube.exception.UserNotFoundException;
import com.buguagaoshu.tiktube.exception.VerifyFailedException;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import com.buguagaoshu.tiktube.service.*;
import com.buguagaoshu.tiktube.utils.*;
import com.buguagaoshu.tiktube.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.dao.UserDao;
import com.buguagaoshu.tiktube.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Slf4j
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    /**
     * 未设置记住我时 token 过期时间
     */
    private static final long EXPIRATION_TIME = 7200000;

    /**
     * 记住我时 cookie token 过期时间
     */
    private static final int COOKIE_EXPIRATION_TIME = 1296000;


    private static final long TEN_MINUTES =  600000;

    /**
     * 密码长度下限
     */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * 密码长度上限，超长输入既无安全收益，又会被部分哈希算法截断
     */
    private static final int MAX_PASSWORD_LENGTH = 64;


    private final UserRoleService userRoleService;


    private final VerifyCodeService verifyCodeService;


    private final LoginLogService loginLogService;

    private final InvitationCodeService invitationCodeService;

    private final FileTableService fileTableService;

    private final TwoFactorAuthenticationServer twoFactorAuthenticationServer;

    private final WebSettingCache webSettingCache;

    private final CountLimitRepository countLimitRepository;

    private final TotpLoginChallengeCache totpLoginChallengeCache;

    private final IpUtil ipUtil;


    @Autowired
    public UserServiceImpl(UserRoleService userRoleService,
                           VerifyCodeService verifyCodeService,
                           LoginLogService loginLogService,
                           InvitationCodeService invitationCodeService,
                           FileTableService fileTableService,
                           TwoFactorAuthenticationServer twoFactorAuthenticationServer,
                           WebSettingCache webSettingCache,
                           CountLimitRepository countLimitRepository,
                           TotpLoginChallengeCache totpLoginChallengeCache,
                           IpUtil ipUtil) {
        this.userRoleService = userRoleService;
        this.verifyCodeService = verifyCodeService;
        this.loginLogService = loginLogService;
        this.invitationCodeService = invitationCodeService;
        this.fileTableService = fileTableService;
        this.twoFactorAuthenticationServer = twoFactorAuthenticationServer;
        this.webSettingCache = webSettingCache;
        this.countLimitRepository = countLimitRepository;
        this.totpLoginChallengeCache = totpLoginChallengeCache;
        this.ipUtil = ipUtil;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UserEntity> page = this.page(
                new Query<UserEntity>().getPage(params),
                new QueryWrapper<UserEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 返回用户列表
     */
    @Override
    public PageUtils userList(Map<String, Object> params) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        String search = (String) params.get("search");
        if (search != null && !search.isEmpty()) {
            wrapper.like("username", search);
        }
        wrapper.orderByDesc("create_time");

        IPage<UserEntity> page = this.page(
                new Query<UserEntity>().getPage(params),
                wrapper
        );

        // 将用户列表转换为只包含用户ID的Set
        Set<Long> userIdSet = page.getRecords().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        Map<Long, UserRoleEntity> userRoleEntityMap = userRoleService.listByUserId(userIdSet);
        List<User> userList = new ArrayList<>();
        for (UserEntity user : page.getRecords()) {
            user.setPassword("");
            User vos = new User();
            BeanUtils.copyProperties(user, vos);
            vos.setUserRoleEntity(userRoleEntityMap.get(vos.getId()));
            userList.add(vos);
        }
        return new PageUtils(userList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public User login(LoginDetails loginDetails, HttpServletRequest request, HttpServletResponse response) {
        // 验证码判断
        verifyCodeService.verify(request.getSession().getId(), loginDetails.getVerifyCode());
        // 判断用户使用的是邮箱还是手机号
        UserEntity userEntity = null;
        if (loginDetails.getUsername().contains("@")) {
            userEntity = findUserByEmail(loginDetails.getUsername());
            // 手机号登录
        } else {
            userEntity = findUserByPhone(loginDetails.getUsername());
        }

        // 登录失败按「来源 IP + 账号」计数，避免任意人用少量错误密码把他人账号锁死
        // 账号不存在时用用户输入的那串值计数，同样限制同一来源不断撞号
        String loginKey = CountLimitRepository.loginKey(ipUtil.getIpAddr(request),
                userEntity == null ? loginDetails.getUsername() : userEntity.getMail());
        boolean b = countLimitRepository.allowLogin(loginKey);
        if (!b) {
            throw new UserNotFoundException("错误次数太多了，请一个小时后再试或重置密码！");
        }

        if (userEntity == null) {
            countLimitRepository.recordFailedAttempt(loginKey);
            throw new UserNotFoundException("请检查用户名或密码!");
        }

        if (PasswordUtil.judgePassword(loginDetails.getPassword(), userEntity.getPassword())) {
            // 如果处于被锁定状态
            if (userEntity.getStatus().equals(TypeCode.USER_LOCK)) {
                // 永久锁定
                if (userEntity.getBlockEndTime().equals(0L)) {
                    throw new UserNotFoundException("账号因违反社区规定，已被永久封禁，如有疑问请联系社区管理员！");
                } else if (userEntity.getBlockEndTime() > System.currentTimeMillis()) {
                    throw new UserNotFoundException("账号因违反社区规定，暂时无法登录，请在：" + MyStringUtils.formatTime(userEntity.getBlockEndTime()) + " 后登录！");
                } else {
                    // 解锁
                    userEntity.setStatus(TypeCode.USER_NOT_LOCK);
                    userEntity.setBlockEndTime(null);
                    this.updateById(userEntity);
                }
            }

            // 判断是否开启了两步认证
            if (userEntity.getOtp().equals(TwoFactorAuthenticationType.ClOSE)) {
                return loginSuccess(userEntity, Boolean.TRUE.equals(loginDetails.getRememberMe()), response, request);
            } else {
                // 处理两步认证
                // 待验证状态只保存在服务端，返回给浏览器的只是一个随机 ID
                boolean rememberMe = Boolean.TRUE.equals(loginDetails.getRememberMe());
                String challengeId = totpLoginChallengeCache.create(userEntity.getId(),
                        rememberMe, System.currentTimeMillis() + TEN_MINUTES);
                User user = new User();
                user.setLoginStatus(false);
                user.setOtp(1);
                user.setKey(challengeId);
                return user;
            }
        } else {
            // 错误次数记录
            countLimitRepository.recordFailedAttempt(loginKey);
            throw new UserNotFoundException("请检查用户名或密码!");
        }

    }

    @Override
    public User loginTOTP(TOTPLoginKey userTotpLogin, HttpServletRequest request, HttpServletResponse response) {
        String challengeId = userTotpLogin.getKey();
        TotpLoginChallengeCache.Challenge challenge = totpLoginChallengeCache.consume(challengeId);
        if (challenge == null) {
            throw new UserNotFoundException("登录信息已过期，请返回重新登录！");
        }
        // 用户信息一律重新从数据库读取，绝不信任客户端传来的用户实体
        UserEntity userEntity = this.getById(challenge.getUserId());
        if (userEntity == null || userEntity.getOtpSecret() == null) {
            totpLoginChallengeCache.invalidate(challengeId);
            throw new UserNotFoundException("登录信息已过期，请返回重新登录！");
        }
        if (!twoFactorAuthenticationServer.verifyTOTPCode(userEntity.getId(), userEntity.getOtpSecret(), userTotpLogin.getCode())) {
            // 错误次数记录
            countLimitRepository.recordFailedAttempt(
                    CountLimitRepository.loginKey(ipUtil.getIpAddr(request), userEntity.getMail()));
            throw new UserNotFoundException("验证码错误!");
        }
        totpLoginChallengeCache.invalidate(challengeId);
        return loginSuccess(userEntity, challenge.isRememberMe(), response, request);
    }

    public User loginSuccess(UserEntity userEntity,
                             boolean rememberMe,
                             HttpServletResponse response,
                             HttpServletRequest request) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        long time = System.currentTimeMillis();
        long expirationTime;
        int cookExpirationTime = -1;
        String jwt;
        UserRoleEntity userRoleEntity = userRoleService.findByUserId(userEntity.getId());
        if (userRoleEntity.getRole().equals(RoleTypeEnum.VIP.getRole())) {
            if (userRoleEntity.getVipStopTime() < System.currentTimeMillis()) {
                userRoleEntity.setRole(RoleTypeEnum.USER.getRole());
                userRoleService.updateById(userRoleEntity);
            }
        }

        user.setUserRoleEntity(userRoleEntity);
        if (rememberMe) {
            expirationTime = COOKIE_EXPIRATION_TIME * 1000 + time;
            cookExpirationTime = COOKIE_EXPIRATION_TIME;
        } else {
            expirationTime = EXPIRATION_TIME + time;
        }

        user.setExpireTime(expirationTime);
        user.setPassword("");
        jwt = JwtUtil.createJwt(userEntity.getMail(), String.valueOf(userEntity.getId()), userRoleEntity.getRole(), expirationTime, userRoleEntity.getVipStopTime());
        // 传递 token
        Cookie cookie = new Cookie(WebConstant.COOKIE_TOKEN, jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookExpirationTime);
        response.addCookie(cookie);
        // 写入登录日志
        loginLogService.saveLoginLog(userEntity, request);
        user.setLoginStatus(true);
        // 重置本来源 IP 的登录失败次数
        countLimitRepository.recordSuccess(
                CountLimitRepository.loginKey(ipUtil.getIpAddr(request), userEntity.getMail()));
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum register(UserEntity userEntity, HttpServletRequest request) {
        // 验证码校验在关闭邮箱验证码时启用，启用邮箱验证码后以邮箱验证码为主
        if (!webSettingCache.getWebConfigData().getOpenEmail()) {
            verifyCodeService.verify(request.getSession().getId(), userEntity.getVerifyCode());
        }

        if (userEntity.getPassword() == null
                || userEntity.getPassword().length() < MIN_PASSWORD_LENGTH
                || userEntity.getPassword().length() > MAX_PASSWORD_LENGTH) {
            return ReturnCodeEnum.PASSWORD_TO_SHORT;
        }
        UserEntity sys = findUserByEmail(userEntity.getMail());
        // 检验手机号邮箱是否已被使用
        if (sys != null) {
            return ReturnCodeEnum.USER_EMAIL_ALREADY_EXISTS;
        }
        if (userEntity.getPhone() != null && VerifyFieldUtil.phoneNumber(userEntity.getPhone())) {
            sys = findUserByPhone(userEntity.getPhone());
            if (sys != null) {
                return ReturnCodeEnum.USER_PHONE_ALREADY_EXISTS;
            }
        }
        // 检查用户名
        // 管理员注册
        // TODO 局域网免费看
        if ("admin".equals(userEntity.getUsername())) {
            sys = findUserByUsername(userEntity.getUsername());
            if (sys != null) {
                return ReturnCodeEnum.USER_ALREADY_EXISTS;
            }
            UserEntity user = createRegisterUser(userEntity);
            save(user);
            userRoleService.saveUserRole(user, RoleTypeEnum.ADMIN.getRole(), 0);
            // 普通用户
        } else {
            // 邀请码校验
            InvitationCodeEntity check = invitationCodeService.check(userEntity.getInvitationCode());
            if (webSettingCache.getWebConfigData().getOpenEmail()) {
                verifyCodeService.verify(userEntity.getMail(), userEntity.getEmailCode());
            }

            UserEntity user = createRegisterUser(userEntity);
            // 保存信息
            save(user);
            userRoleService.saveUserRole(user, RoleTypeEnum.USER.getRole(), 0);
            if (check.getId() != -1L) {
                check.setUseUser(user.getId());
                invitationCodeService.updateById(check);
            }

        }
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public Map<Long, UserEntity> userMapList(Set<Long> userIdList) {
        return this.listByIds(userIdList).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

    }

    @Override
    public User userInfo(Long userId) {
        UserEntity userEntity = this.getById(userId);
        if (userEntity == null) {
            User user = new User();
            user.setId(-1L);
            return user;
        }
        clean(userEntity);
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        UserRoleEntity userRoleEntity = userRoleService.findByUserId(userEntity.getId());
        user.setUserRoleEntity(userRoleEntity);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updateAvatar(User user, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        // 检查头像数据是否需要修改
        if (user.getFileId() != null) {
            boolean result = fileTableService.updateFileStatus(userId, user.getFileId(), FileTypeEnum.AVATAR.getCode(), user.getAvatarUrl());
            if (result) {
                userEntity.setAvatarUrl(user.getAvatarUrl());
                this.updateById(userEntity);
                return ReturnCodeEnum.SUCCESS;
            }
        }
        return ReturnCodeEnum.DATA_VALID_EXCEPTION;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updateTopImage(User user, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        // 检查首页大图数据是否需要修改
        if (user.getFileId() != null) {
            boolean result = fileTableService.updateFileStatus(userId, user.getFileId(), FileTypeEnum.TOP_IMAGE.getCode(), user.getTopImgUrl());
            if (result) {
                userEntity.setTopImgUrl(user.getTopImgUrl());
                this.updateById(userEntity);
                return ReturnCodeEnum.SUCCESS;
            }
        }
        return ReturnCodeEnum.DATA_VALID_EXCEPTION;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updatePassword(PasswordDto passwordDto,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        // 验证码校验
        verifyCodeService.verify(request.getSession().getId(), passwordDto.getVerifyCode());
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        UserEntity userEntity = this.getById(userId);
        if (userEntity == null) {
            return ReturnCodeEnum.USER_NOT_FIND;
        }
        if (PasswordUtil.judgePassword(passwordDto.getOldPassword(), userEntity.getPassword())) {
            String newPassword = passwordDto.getNewPassword();
            if (newPassword == null
                    || newPassword.length() < MIN_PASSWORD_LENGTH
                    || newPassword.length() > MAX_PASSWORD_LENGTH) {
                return ReturnCodeEnum.PASSWORD_TO_SHORT;
            }
            UserEntity newUser = new UserEntity();

            newUser.setId(userId);
            newUser.setPassword(PasswordUtil.encode(passwordDto.getNewPassword()));

            this.updateById(newUser);
            Cookie cookie = WebUtils.getCookie(request, WebConstant.COOKIE_TOKEN);
            cookie.setValue(null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return ReturnCodeEnum.SUCCESS;
        } else {
            throw new UserNotFoundException("原密码错误！");
        }

    }

    @Override
    public ReturnCodeEnum updateInfo(User user, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        UserEntity userEntity = new UserEntity();
        if (StringUtils.hasText(user.getUsername())) {
            if (user.getUsername().length() > 25) {
                return ReturnCodeEnum.USER_NAME_TO_LONG;
            }
            userEntity.setUsername(user.getUsername());
        }
        if (StringUtils.hasText(user.getIntroduction())) {
            if (user.getIntroduction().length() > 100) {
                return ReturnCodeEnum.USER_INTRODUCTION_LONG;
            }
            userEntity.setIntroduction(user.getIntroduction());
        }
        UserEntity sys = this.getById(userId);
        if (sys == null) {
            return ReturnCodeEnum.USER_NOT_FIND;
        }
        if (sys.getUsername().equals("admin") && !"admin".equals(user.getUsername())) {
            return ReturnCodeEnum.ADMIN_DONT_RENAME;
        }
        if ("admin".equals(user.getUsername()) && !sys.getUsername().equals("admin")) {
            return ReturnCodeEnum.DONT_USER_NAME;
        }
        userEntity.setId(userId);
        this.updateById(userEntity);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public void addSubmitCount(Long userId, int count) {
        this.baseMapper.addSubmitCount(userId, count);
    }

    @Override
    public UserRoleEntity updateRole(UserRoleEntity userRole, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        UserRoleEntity roleEntity = userRoleService.findByUserId(userRole.getUserid());
        if (roleEntity == null) {
            return null;
        }
        if (RoleTypeEnum.check(userRole.getRole())) {
            userRole.setId(roleEntity.getId());
            userRole.setUpdateTime(System.currentTimeMillis());
            userRole.setModified(userId);
            userRoleService.updateById(userRole);

            return userRole;
        }
        return null;
    }

    @Override
    public String resetPassword(UserEntity userEntity) {
        // 查找用户
        UserEntity user = this.getById(userEntity.getId());
        if (user == null) {
            return "";
        }
        String newPwd = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 10);
        user.setPassword(PasswordUtil.encode(newPwd));
        this.updateById(user);
        return newPwd;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum addUser(AdminAddUserData userEntity, HttpServletRequest request) {
        long adminID = JwtUtil.getUserId(request);
        long time = System.currentTimeMillis();
        if (!RoleTypeEnum.check(userEntity.getRole())) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }
        UserEntity sys = findUserByEmail(userEntity.getMail());
        // 检验手机号邮箱是否已被使用
        if (sys != null) {
            return ReturnCodeEnum.USER_EMAIL_ALREADY_EXISTS;
        }
        if (userEntity.getPhone() != null && VerifyFieldUtil.phoneNumber(userEntity.getPhone())) {
            sys = findUserByPhone(userEntity.getPhone());
            if (sys != null) {
                return ReturnCodeEnum.USER_PHONE_ALREADY_EXISTS;
            }
        }
        UserEntity user = new UserEntity();
        user.setPhone(userEntity.getPhone());
        user.setMail(userEntity.getMail());
        user.setCreateTime(time);
        user.setUsername(userEntity.getUsername());
        user.setPassword(PasswordUtil.encode(userEntity.getPassword()));
        user.setAvatarUrl("/images/head.png");
        user.setTopImgUrl("/images/top.png");
        save(user);
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setModified(adminID);
        userRoleEntity.setCreateTime(time);
        userRoleEntity.setUpdateTime(time);
        userRoleEntity.setUserid(user.getId());
        userRoleEntity.setRole(userEntity.getRole());
        userRoleEntity.setVipStartTime(userEntity.getVipStartTime());
        userRoleEntity.setVipStopTime(userEntity.getVipStopTime());
        userRoleService.save(userRoleEntity);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public void addCount(String col, Long userId, int count) {
        // col 会被拼进 update 语句，只允许计数列白名单
        this.baseMapper.addCount(SQLFilter.checkedCountColumn(col), userId, count);
    }

    @Override
    public void updateLastPublishTime(long time, long userId) {
        this.baseMapper.updateLastPublishTime(time, userId);
    }


    @Override
    public TwoFactorData openTOTP(Long userId, boolean checkOpen) {
        UserEntity user = this.getById(userId);
        if (user == null) {
            return null;
        }
        if (user.getOtp().equals(TwoFactorAuthenticationType.TOTP)) {
            return null;
        }
        return updateTOTP(user);
    }

    @Override
    public boolean closeTOTP(TwoFactorData twoFactorData, Long userId) {
        UserEntity user = this.getById(userId);
        if (user == null) {
            return false;
        }
        if (!user.getOtp().equals(TwoFactorAuthenticationType.TOTP)) {
            return false;
        }
        boolean code = twoFactorAuthenticationServer.verifyTOTPCode(userId, user.getOtpSecret(), twoFactorData.getCode());
        if (code) {
            user.setOtp(TwoFactorAuthenticationType.ClOSE);
            user.setOtpRecovery(null);
            user.setOtpRecovery(null);
            this.updateById(user);
            return true;
        }
        return false;
    }

    public TwoFactorData updateTOTP(UserEntity user) {
        TwoFactorData totpInfo = twoFactorAuthenticationServer.createTOTPInfo(user.getId(), user.getMail());
        user.setOtp(TwoFactorAuthenticationType.TOTP);
        user.setOtpSecret(totpInfo.getSecret());
        user.setOtpRecovery(PasswordUtil.encode(totpInfo.getRecoveryCode()));
        this.updateById(user);
        return totpInfo;
    }

    @Override
    public TwoFactorData recoveryTOTP(TwoFactorData twoFactorData, long userId) {
        // 校验用户恢复码
        UserEntity user = this.getById(userId);
        if (PasswordUtil.judgePassword(twoFactorData.getRecoveryCode(), user.getOtpRecovery())) {
            return updateTOTP(user);
        }
        return null;
    }

    @Override
    public boolean checkTOTP(TwoFactorData twoFactorData, long userId) {
        UserEntity user = this.getById(userId);
        return twoFactorAuthenticationServer.verifyTOTPCode(userId, user.getOtpSecret(), twoFactorData.getCode());
    }

    @Override
    public TwoFactorData createNewTOTP(TwoFactorData twoFactorData, long userId) {
        UserEntity user = this.getById(userId);
        boolean code = twoFactorAuthenticationServer.verifyTOTPCode(userId, user.getOtpSecret(), twoFactorData.getCode());
        if (code) {
            return updateTOTP(user);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockUser(UserEntity userEntity, long adminUserId) {
        UserEntity u = this.getById(userEntity.getId());
        if (u == null) {
            return false;
        }
        if (adminUserId == u.getId()) {
            return false;
        }
        u.setStatus(userEntity.getStatus());
        u.setBlockEndTime(userEntity.getBlockEndTime());
        this.updateById(u);

        return true;
    }

    @Override
    public ReturnCodeEnum forgotPassword(UserEntity user, String sessionId) {
        String mail = user.getMail();
        String newPassword = user.getPassword();
        if (mail == null || mail.isEmpty()) {
            return ReturnCodeEnum.VERIFY_FAILED;
        }
        // 密码长度只取决于调用方自己的输入，可以单独报错
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return ReturnCodeEnum.PASSWORD_TO_SHORT;
        }
        if (newPassword.length() > MAX_PASSWORD_LENGTH) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }

        UserEntity userEntity = null;
        try {
            verifyCodeService.verify(mail, user.getEmailCode());
            userEntity = findUserByEmail(mail);
        } catch (VerifyFailedException ignored) {
            // 与「邮箱未注册」合并为同一返回值
        }
        if (userEntity == null) {
            // 验证码错误和邮箱未注册返回完全一致的结果
            // 发送验证码接口对任意邮箱都会发送，若此处区分提示将可用于枚举已注册账号
            return ReturnCodeEnum.VERIFY_FAILED;
        }

        userEntity.setPassword(PasswordUtil.encode(newPassword));
        this.updateById(userEntity);
        // 邮箱验证码已通过，说明确实持有该邮箱，解除该账号所有来源 IP 的登录锁定
        countLimitRepository.clearAccount(mail);
        return ReturnCodeEnum.SUCCESS;
    }


    public UserEntity findUserByEmail(String email) {
        return this.getOne(new QueryWrapper<UserEntity>().eq("mail", email));
    }


    public UserEntity findUserByPhone(String phone) {
        return this.getOne(new QueryWrapper<UserEntity>().eq("phone", phone));
    }

    public UserEntity findUserByUsername(String username) {
        return this.getOne(new QueryWrapper<UserEntity>().eq("username", username));
    }

    public UserEntity createRegisterUser(UserEntity user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setCreateTime(System.currentTimeMillis());

        userEntity.setMail(user.getMail());
        userEntity.setUsername(user.getUsername());
        userEntity.setPhone(user.getPhone());
        userEntity.setPassword(PasswordUtil.encode(user.getPassword()));
        userEntity.setAvatarUrl("/images/head.png");
        userEntity.setTopImgUrl("/images/top.png");
        return userEntity;
    }


    /**
     * 去除敏感信息
     */
    public void clean(UserEntity userEntity) {
        userEntity.setPassword("");
        userEntity.setMail("");
        userEntity.setPhone("");
    }

}