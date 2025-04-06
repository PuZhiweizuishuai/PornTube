package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.dto.LoginDetails;
import com.buguagaoshu.porntube.dto.PasswordDto;
import com.buguagaoshu.porntube.entity.InvitationCodeEntity;
import com.buguagaoshu.porntube.entity.UserRoleEntity;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.enums.RoleTypeEnum;
import com.buguagaoshu.porntube.exception.UserNotFoundException;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.*;
import com.buguagaoshu.porntube.vo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.dao.UserDao;
import com.buguagaoshu.porntube.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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


    private final UserRoleService userRoleService;


    private final VerifyCodeService verifyCodeService;


    private final LoginLogService loginLogService;

    private final InvitationCodeService invitationCodeService;

    private final FileTableService fileTableService;

    @Autowired
    public UserServiceImpl(UserRoleService userRoleService, VerifyCodeService verifyCodeService, LoginLogService loginLogService, InvitationCodeService invitationCodeService, FileTableService fileTableService) {
        this.userRoleService = userRoleService;
        this.verifyCodeService = verifyCodeService;
        this.loginLogService = loginLogService;
        this.invitationCodeService = invitationCodeService;
        this.fileTableService = fileTableService;
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
     * */
    @Override
    public PageUtils userList(Map<String, Object> params) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
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
        for (UserEntity user :  page.getRecords()) {
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
        if (userEntity == null) {
            throw new UserNotFoundException("请检查用户名或密码!");
        }

        if (PasswordUtil.judgePassword(loginDetails.getPassword(), userEntity.getPassword())) {
            User user = new User();
            BeanUtils.copyProperties(userEntity, user);
            long time = System.currentTimeMillis();
            long expirationTime = EXPIRATION_TIME;
            int cookExpirationTime = -1;
            String jwt = "";
            UserRoleEntity userRoleEntity = userRoleService.findByUserId(userEntity.getId());
            if (userRoleEntity.getRole().equals(RoleTypeEnum.VIP.getRole())) {
                if (userRoleEntity.getVipStopTime() < System.currentTimeMillis()) {
                    userRoleEntity.setRole(RoleTypeEnum.USER.getRole());
                    userRoleService.updateById(userRoleEntity);
                }
            }

            user.setUserRoleEntity(userRoleEntity);
            if (loginDetails.getRememberMe() != null && loginDetails.getRememberMe()) {
                expirationTime = COOKIE_EXPIRATION_TIME * 1000 + time;
                cookExpirationTime = COOKIE_EXPIRATION_TIME;
            } else {
                expirationTime += time;
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
            return user;
        } else {
            throw new UserNotFoundException("请检查用户名或密码!");
        }

    }

    @Override
    @Transactional
    public ReturnCodeEnum register(UserEntity userEntity, HttpServletRequest request) {
        // 验证码校验
        verifyCodeService.verify(request.getSession().getId(), userEntity.getVerifyCode());
        if (userEntity.getPassword().length() < 6) {
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
            UserEntity user = createRegisterUser(userEntity);
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
            fileTableService.updateFileStatus(userId, user.getFileId(), FileTypeEnum.AVATAR.getCode(), user.getAvatarUrl());
        }
        userEntity.setAvatarUrl(user.getAvatarUrl());
        this.updateById(userEntity);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updateTopImage(User user, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        // 检查首页大图数据是否需要修改
        if (user.getFileId() != null) {
            fileTableService.updateFileStatus(userId, user.getFileId(), FileTypeEnum.TOP_IMAGE.getCode(), user.getTopImgUrl());
        }
        userEntity.setTopImgUrl(user.getTopImgUrl());
        this.updateById(userEntity);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
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
            if (passwordDto.getNewPassword().length() < 6) {
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
        if (StringUtils.hasText(user.getUsername()) ) {
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
            return  ReturnCodeEnum.DONT_USER_NAME;
        }
        userEntity.setId(userId);
        this.updateById(userEntity);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public void addSubmitCount(Long userId, int count) {
        this.baseMapper.addSubmitCount(userId, count);
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
     * */
    public void clean(UserEntity userEntity) {
        userEntity.setPassword("");
        userEntity.setMail("");
        userEntity.setPhone("");
    }

}