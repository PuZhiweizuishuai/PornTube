package com.buguagaoshu.porntube.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 15:22
 */
@Data
public class PasswordDto {
    @NotBlank(message = "请输入原始密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    private String newPassword;


    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
