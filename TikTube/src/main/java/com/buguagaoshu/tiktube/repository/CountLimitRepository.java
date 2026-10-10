package com.buguagaoshu.tiktube.repository;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 * 存储次数限制信息
 */
public interface CountLimitRepository {

    /**
     * 登录失败计数 key 的分隔符，key 形如 ip|账号
     */
    String LOGIN_KEY_SEPARATOR = "|";

    /**
     * 登录失败计数的 key
     * 必须带上来源 IP，否则任何人只要知道邮箱就能把该账号锁定，
     * 反过来攻击者也无法用少量失败请求把受害者的账号从其他网络锁死
     *
     * @param ip      请求来源 IP
     * @param account 账号（邮箱）
     */
    static String loginKey(String ip, String account) {
        return (ip == null ? "" : ip) + LOGIN_KEY_SEPARATOR + account;
    }

    /**
     * 登录次数限制
     * */
    boolean allowLogin(String key);

    void recordFailedAttempt(String key);

    void recordSuccess(String key);

    int getRemainingAttempts(String key);

    /**
     * 清除该账号在所有 IP 上的失败计数与锁定
     * 重置密码成功（已证明持有邮箱）后调用
     *
     * @param account 账号（邮箱）
     */
    void clearAccount(String account);
    
    /**
     * 检查是否允许发送邮件
     * @param key 邮箱地址或IP地址
     * @return 是否允许发送
     */
    boolean allowSendEmail(String key);
    
    /**
     * 记录一次邮件发送
     * @param key 邮箱地址或IP地址
     */
    void recordEmailSent(String key);
    
    /**
     * 获取剩余邮件发送次数
     * @param key 邮箱地址或IP地址
     * @return 剩余次数
     */
    int getRemainingEmailSendAttempts(String key);
}
