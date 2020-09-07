package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.dto.LoginDetails;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.service.UserService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/user/info/{id}")
    public ResponseDetails userInfo(@PathVariable("id") Long userId) {
        return ResponseDetails.ok().put("data", userService.userInfo(userId));
    }
}
