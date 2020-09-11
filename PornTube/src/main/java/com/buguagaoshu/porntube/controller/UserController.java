package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.dto.LoginDetails;
import com.buguagaoshu.porntube.dto.PasswordDto;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.service.UserService;
import com.buguagaoshu.porntube.service.impl.UserServiceImpl;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import com.buguagaoshu.porntube.vo.User;
import com.sun.net.httpserver.HttpsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
}
