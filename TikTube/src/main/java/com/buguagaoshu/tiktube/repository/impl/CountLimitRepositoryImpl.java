package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.model.EmailSendCount;
import com.buguagaoshu.tiktube.model.LoginCount;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 */
@Repository("countLimitRepositoryImpl")
public class CountLimitRepositoryImpl implements CountLimitRepository {

    private final ConcurrentMap<String, LoginCount> loginCount = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, EmailSendCount> emailSendCount = new ConcurrentHashMap<>();

    /**
     * 记录数上限，超过则清理过期记录，仍超限则整体清空
     * 防止攻击者用海量不同 key 把内存撑爆
     */
    private static final int MAX_RECORDS = 20000;

    /**
     * 清理间隔（毫秒）
     */
    private static final long PURGE_INTERVAL = 60 * 1000L;

    private volatile long lastPurgeTime = 0L;

    private void purge() {
        long now = System.currentTimeMillis();
        if (loginCount.size() < MAX_RECORDS && emailSendCount.size() < MAX_RECORDS
                && now - lastPurgeTime < PURGE_INTERVAL) {
            return;
        }
        lastPurgeTime = now;
        loginCount.entrySet().removeIf(entry -> entry.getValue().isExpired());
        emailSendCount.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (loginCount.size() >= MAX_RECORDS) {
            loginCount.clear();
        }
        if (emailSendCount.size() >= MAX_RECORDS) {
            emailSendCount.clear();
        }
    }

    @Override
    public boolean allowLogin(String key) {
        purge();
        LoginCount count = loginCount.get(key);
        if (count == null) {
            return true;
        }
        if (count.isExpired()) {
            loginCount.remove(key, count);
            return true;
        }
        return !count.isLocked();
    }

    /**
     * 记录一次登录失败
     */
    @Override
    public void recordFailedAttempt(String key) {
        purge();
        loginCount.compute(key, (k, existing) -> {
            LoginCount attempt = existing == null ? new LoginCount() : existing;
            if (attempt.isExpired()) {
                attempt.reset();
            }
            attempt.increment();
            if (attempt.getAttempts() >= WebConstant.MAX_ATTEMPTS) {
                attempt.lock();
            }
            return attempt;
        });
    }

    /**
     * 记录登录成功，重置尝试次数
     */
    @Override
    public void recordSuccess(String key) {
        loginCount.remove(key);
    }

    /**
     * 获取账号在当前来源 IP 上的剩余尝试次数
     */
    @Override
    public int getRemainingAttempts(String key) {
        LoginCount attempt = loginCount.get(key);
        return attempt != null ? Math.max(0, WebConstant.MAX_ATTEMPTS - attempt.getAttempts()) : WebConstant.MAX_ATTEMPTS;
    }

    @Override
    public void clearAccount(String account) {
        String suffix = LOGIN_KEY_SEPARATOR + account;
        loginCount.keySet().removeIf(key -> key.endsWith(suffix));
    }

    /**
     * 检查是否允许发送邮件
     */
    @Override
    public boolean allowSendEmail(String key) {
        purge();
        EmailSendCount count = emailSendCount.computeIfAbsent(key, k -> new EmailSendCount());
        return !count.isLimited();
    }

    /**
     * 记录一次邮件发送
     */
    @Override
    public void recordEmailSent(String key) {
        purge();
        emailSendCount.computeIfAbsent(key, k -> new EmailSendCount()).increment();
    }

    /**
     * 获取剩余邮件发送次数
     */
    @Override
    public int getRemainingEmailSendAttempts(String key) {
        EmailSendCount count = emailSendCount.get(key);
        return count != null ? count.getRemainingAttempts() : new EmailSendCount().getRemainingAttempts();
    }
}
