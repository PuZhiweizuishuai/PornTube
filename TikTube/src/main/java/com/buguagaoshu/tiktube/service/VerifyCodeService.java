package com.buguagaoshu.tiktube.service;

import java.awt.*;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-26 17:05
 * 验证码验证接口
 */
public interface VerifyCodeService {

    void send(String key);

    void verify(String key, String code);

    Image image(String key);
}
