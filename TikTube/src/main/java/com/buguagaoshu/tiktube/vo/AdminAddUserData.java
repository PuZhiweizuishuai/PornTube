package com.buguagaoshu.tiktube.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-18
 */
@Data
public class AdminAddUserData {
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    String mail;

    @NotBlank(message = "用户名不能为空")
    String username;

    @NotBlank(message = "密码不能为空")
    String password;

    String role;

    String phone;

    Long vipStartTime;

    Long vipStopTime;
}
