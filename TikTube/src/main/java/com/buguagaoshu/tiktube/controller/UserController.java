package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.LoginDetails;
import com.buguagaoshu.tiktube.dto.PasswordDto;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.entity.UserRoleEntity;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.vo.AdminAddUserData;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import com.buguagaoshu.tiktube.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 14:48
 */
@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/api/login")
    public ResponseDetails login(@Valid @RequestBody LoginDetails loginDetails,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        return ResponseDetails.ok().put("data", userService.login(loginDetails, request, response));
    }


    @PostMapping("/api/register")
    public ResponseDetails register(@Valid @RequestBody UserEntity userEntity, HttpServletRequest request) {
        return ResponseDetails.ok(userService.register(userEntity, request));
    }

    @GetMapping("/api/logout")
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

    @PostMapping("/api/user/update/avatar")
    public ResponseDetails updateUserAvatar(@RequestBody User user,
                                            HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateAvatar(user, request));
    }

    @PostMapping("/api/user/update/top")
    public ResponseDetails updateTopImage(@RequestBody User user,
                                          HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateTopImage(user, request));
    }

    @PostMapping("/api/user/update/password")
    public ResponseDetails updatePassword(@RequestBody PasswordDto passwordDto,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        return ResponseDetails.ok(userService.updatePassword(passwordDto, request, response));
    }


    @PostMapping("/api/user/update/info")
    public ResponseDetails updateInfo(@RequestBody User user,
                                      HttpServletRequest request) {
        return ResponseDetails.ok(userService.updateInfo(user, request));
    }


    /**
     * 管理员读取用户列表
     * */
    @GetMapping("/api/admin/user/list")
    public ResponseDetails userList(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", userService.userList(params));
    }

    @PostMapping("/api/admin/user/update/role")
    public ResponseDetails updateUserRole(@RequestBody UserRoleEntity userRole,
                                          HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.updateRole(userRole, request));
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


}
