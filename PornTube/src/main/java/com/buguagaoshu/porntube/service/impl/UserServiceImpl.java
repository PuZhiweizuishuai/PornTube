package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.dto.LoginDetails;
import com.buguagaoshu.porntube.entity.InvitationCodeEntity;
import com.buguagaoshu.porntube.entity.UserRoleEntity;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.enums.RoleTypeEnum;
import com.buguagaoshu.porntube.exception.UserNotFoundException;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.*;
import com.buguagaoshu.porntube.vo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.dao.UserDao;
import com.buguagaoshu.porntube.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    public static final String COOKIE_TOKEN = "COOKIE-TOKEN";
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

    @Autowired
    public UserServiceImpl(UserRoleService userRoleService, VerifyCodeService verifyCodeService, LoginLogService loginLogService, InvitationCodeService invitationCodeService) {
        this.userRoleService = userRoleService;
        this.verifyCodeService = verifyCodeService;
        this.loginLogService = loginLogService;
        this.invitationCodeService = invitationCodeService;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UserEntity> page = this.page(
                new Query<UserEntity>().getPage(params),
                new QueryWrapper<UserEntity>()
        );

        return new PageUtils(page);
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
            user.setUserRoleEntity(userRoleEntity);
            if (loginDetails.getRememberMe() != null && loginDetails.getRememberMe()) {
                expirationTime = COOKIE_EXPIRATION_TIME * 1000 + time;
                cookExpirationTime = COOKIE_EXPIRATION_TIME;
            }

            user.setExpireTime(expirationTime);
            user.setPassword("");
            jwt = JwtUtil.createJwt(userEntity.getMail(), String.valueOf(userEntity.getId()), userRoleEntity.getRole(), expirationTime);
            // 传递 token
            Cookie cookie = new Cookie(COOKIE_TOKEN, jwt);
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