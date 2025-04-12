package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.LoginLogService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 15:56
 * 登录历史
 */
@RestController
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Autowired
    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping("/api/login/log/list")
    public ResponseDetails logList(HttpServletRequest request,
                                   @RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", loginLogService.queryPage(request, params));
    }
}
