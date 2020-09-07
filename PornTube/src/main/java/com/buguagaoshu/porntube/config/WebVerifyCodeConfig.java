package com.buguagaoshu.porntube.config;


import com.buguagaoshu.porntube.utils.VerifyCodeUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-06-06 16:39
 * 验证码配置
 */
@Configuration
public class WebVerifyCodeConfig {
    @Bean
    public VerifyCodeUtil verifyCodeUtil() {
        return new VerifyCodeUtil();
    }
}
