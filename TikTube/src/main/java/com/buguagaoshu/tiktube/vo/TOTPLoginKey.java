package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-25
 * 两步认证登录请求，key 为登录接口下发的随机 ID
 */
@Data
public class TOTPLoginKey {
    private String code;

    private String key;
}
