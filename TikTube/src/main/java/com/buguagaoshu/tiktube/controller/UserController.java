package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.LoginDetails;
import com.buguagaoshu.tiktube.dto.PasswordDto;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.entity.UserRoleEntity;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.enums.RoleTypeEnum;
import com.buguagaoshu.tiktube.enums.TypeCode;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.utils.MyStringUtils;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.vo.AdminAddUserData;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import com.buguagaoshu.tiktube.vo.TOTPLoginKey;
import com.buguagaoshu.tiktube.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 14:48
 */
@RestController
public class UserController {
    private final UserService userService;

    private final NotificationService notificationService;

    @Autowired
    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }


    @PostMapping("/api/login")
    public ResponseDetails login(@Valid @RequestBody LoginDetails loginDetails,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        return ResponseDetails.ok().put("data", userService.login(loginDetails, request, response));
    }

    @PostMapping("/api/login/totp")
    public ResponseDetails loginTOTP(@RequestBody TOTPLoginKey totpLoginKey,
                                     HttpServletResponse response,
                                     HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.loginTOTP(totpLoginKey, request, response));
    }


    @PostMapping("/api/register")
    public ResponseDetails register(@Valid @RequestBody UserEntity userEntity, HttpServletRequest request) {
        return ResponseDetails.ok(userService.register(userEntity, request));
    }

    @PostMapping("/api/logout")
    public ResponseDetails logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, WebConstant.COOKIE_TOKEN);
        if (cookie == null) {
            return ResponseDetails.ok(0, "没有登录");
        }
        cookie.setValue(null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseDetails.ok(200, "退出成功！");
    }

    @GetMapping("/api/user/info/{id}")
    public ResponseDetails userInfo(@PathVariable("id") Long userId) {
        return ResponseDetails.ok().put("data", userService.userInfo(userId));
    }

    @PostMapping("/api/user/forgot/password")
    public ResponseDetails forgotPassword(@RequestBody UserEntity user) {
        return ResponseDetails.ok(userService.forgotPassword(user));
    }

    @PostMapping("/api/user/update/avatar")
    public ResponseDetails updateUserAvatar(@Valid @RequestBody User user,
                                            HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateAvatar(user, request));
    }

    @PostMapping("/api/user/update/top")
    public ResponseDetails updateTopImage(@Valid @RequestBody User user,
                                          HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateTopImage(user, request));
    }

    @PostMapping("/api/user/update/password")
    public ResponseDetails updatePassword(@Valid @RequestBody PasswordDto passwordDto,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        return ResponseDetails.ok(userService.updatePassword(passwordDto, request, response));
    }


    @PostMapping("/api/user/update/info")
    public ResponseDetails updateInfo(@Valid @RequestBody User user,
                                      HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateInfo(user, request));
    }

    @GetMapping("/api/user/list/search")
    public ResponseDetails searchUser(@RequestParam Map<String, Object> params) {
        String search = (String) params.get("search");
        if (search != null && !search.isEmpty()) {
            PageUtils pageUtils = userService.userList(params);
            List<User> list = (List<User>) pageUtils.getList();
            list.forEach(u -> u.setMail(""));
            return ResponseDetails.ok().put("data", pageUtils);
        }
        return ResponseDetails.ok();
    }


    /**
     * 管理员读取用户列表
     */
    @GetMapping("/api/admin/user/list")
    public ResponseDetails userList(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", userService.userList(params));
    }

    @PostMapping("/api/admin/user/update/role")
    public ResponseDetails updateUserRole(@RequestBody UserRoleEntity userRole,
                                          HttpServletRequest request) {
        UserRoleEntity role = userService.updateRole(userRole, request);
        if (role != null && role.getRole().equals(RoleTypeEnum.VIP.getRole())) {
            notificationService.sendNotification(
                    JwtUtil.getUserId(request),
                    role.getUserid(),
                    -1,
                    -1,
                    -1,
                    "恭喜你，已经成为尊贵的 VPI 用户",
                    "",
                    "管理员已经将你设置为 VPI 用户，有效期为："
                            + MyStringUtils.formatTime(userRole.getVipStartTime())
                            + " - "
                            + MyStringUtils.formatTime(userRole.getVipStopTime()),
                    NotificationType.SYSTEM
            );
        }
        return ResponseDetails.ok().put("data", userRole);
    }

    @PostMapping("/api/admin/user/update/pwd")
    public ResponseDetails resetPassword(@RequestBody UserEntity userEntity) {
        return ResponseDetails.ok().put("data", userService.resetPassword(userEntity));
    }

    @PostMapping("/api/admin/user/add")
    public ResponseDetails adminAddUser(@RequestBody AdminAddUserData adminAddUserData,
                                        HttpServletRequest request) {
        return ResponseDetails.ok(userService.addUser(adminAddUserData, request));
    }


    /**
     * 封禁用户
     * */
    @PostMapping("/api/admin/user/lock")
    public ResponseDetails lockUser(@RequestBody UserEntity userEntity, HttpServletRequest request) {
        long adminUserId = JwtUtil.getUserId(request);
        boolean b = userService.lockUser(userEntity, adminUserId);
        String title = "账号已被管理员解封！";
        String content = "由于您近期表现良好，管理员已提前解封您的账号！";
        if (userEntity.getStatus().equals(TypeCode.USER_LOCK)) {
            title = "账号被封禁！";
            if (userEntity.getBlockEndTime().equals(0L)) {
                content = "由于您近期多次违反社区规定，账号已被永久封禁！";
            } else {
                content = "由于您近期多次违反社区规定，账号已被封禁到：" + MyStringUtils.formatTime(userEntity.getBlockEndTime());
            }
        }
        if (b) {
            notificationService.sendNotification(
                    adminUserId,
                    userEntity.getId(),
                    -1,
                    -1,
                    -1,
                    title,
                    "",
                    content,
                    NotificationType.SYSTEM
            );
        }

        return ResponseDetails.ok().put("data", b);
    }
}
