package com.buguagaoshu.tiktube.repository;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:58
 */
public interface VerifyCodeRepository {
    void save(String key, String code);

    /**
     * 保存验证码
     * @param key 验证码 key 值，可能是邮箱或手机号
     * @param code 验证码值
     * @param time 过期时间，单位分钟
     */
    void save(String key, String code, Long time);

    String find(String key);

    void remove(String key);
}
