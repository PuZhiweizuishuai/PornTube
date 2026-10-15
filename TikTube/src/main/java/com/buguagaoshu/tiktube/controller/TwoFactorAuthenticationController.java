package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import com.buguagaoshu.tiktube.vo.TwoFactorData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-25
 */
@RestController
@RequestMapping("/api/2fa")
public class TwoFactorAuthenticationController {

    private final UserService userService;

    @Autowired
    public TwoFactorAuthenticationController(UserService userService) {
        this.userService = userService;
    }


    /**
     * 创建
     * */
    @PostMapping("/totp/create")
    public ResponseDetails create(HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", userService.openTOTP(userId, true));
    }


    @PostMapping("/totp/close")
    public ResponseDetails close(@Valid @RequestBody TwoFactorData twoFactorData, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.closeTOTP(twoFactorData, JwtUtil.getUserId(request)));
    }

    /**
     * 恢复
     * */
    @PostMapping("/totp/recover")
    public ResponseDetails recover(@Valid @RequestBody TwoFactorData twoFactorData, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.recoveryTOTP(twoFactorData, JwtUtil.getUserId(request)));
    }


    /**
     * 重新生成
     * */
    @PostMapping("/totp/new")
    public ResponseDetails newCreate(@Valid @RequestBody TwoFactorData twoFactorData, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.createNewTOTP(twoFactorData, JwtUtil.getUserId(request)));
    }


    /**
     * 检查
     * */
    @PostMapping("/totp/check")
    public ResponseDetails check(@RequestBody TwoFactorData twoFactorData, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.checkTOTP(twoFactorData, JwtUtil.getUserId(request)));
    }

    /**
     * 取消设置：作废待确认密钥，两步验证保持原状态
     * */
    @PostMapping("/totp/cancel")
    public ResponseDetails cancel(HttpServletRequest request) {
        return ResponseDetails.ok().put("data", userService.cancelTOTP(JwtUtil.getUserId(request)));
    }
}
