package com.buguagaoshu.tiktube.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 14:51
 */
@Data
public class LoginDetails {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    private Boolean rememberMe;
}
