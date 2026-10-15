package com.buguagaoshu.tiktube.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-15
 * 开启 / 重新生成 / 恢复两步验证时保存在服务端的待确认密钥。
 * 只有用户扫码后用验证码确认（/totp/check 验证通过）才会落库生效；
 * 弹窗被关闭或超时未确认时，用户原有的两步验证状态不受任何影响。
 */
@Component
public class TotpSetupCache {

    /**
     * 待确认密钥有效期：10 分钟内必须完成扫码 + 验证码确认
     */
    private static final long TTL = 600000L;

    /**
     * 同一个待确认密钥最多允许尝试的验证码次数
     */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final Map<Long, Pending> pendings = new ConcurrentHashMap<>();

    /**
     * 记录（或覆盖）某个用户的待确认密钥
     */
    public void put(Long userId, String secret, String recoveryCode) {
        pendings.put(userId, new Pending(secret, recoveryCode, System.currentTimeMillis() + TTL));
    }

    /**
     * 取出待确认密钥并记一次验证尝试，不存在 / 已过期 / 尝试次数用尽时返回 null
     */
    public Pending take(Long userId) {
        if (userId == null) {
            return null;
        }
        Pending pending = pendings.get(userId);
        if (pending == null) {
            return null;
        }
        if (pending.expireTime < System.currentTimeMillis()
                || pending.attempts.incrementAndGet() > MAX_VERIFY_ATTEMPTS) {
            pendings.remove(userId);
            return null;
        }
        return pending;
    }

    /**
     * 确认成功或用户取消设置时调用，避免同一个待确认密钥被重复使用
     */
    public void invalidate(Long userId) {
        if (userId != null) {
            pendings.remove(userId);
        }
    }

    public static class Pending {
        private final String secret;
        private final String recoveryCode;
        private final long expireTime;
        private final AtomicInteger attempts = new AtomicInteger();

        private Pending(String secret, String recoveryCode, long expireTime) {
            this.secret = secret;
            this.recoveryCode = recoveryCode;
            this.expireTime = expireTime;
        }

        public String getSecret() {
            return secret;
        }

        public String getRecoveryCode() {
            return recoveryCode;
        }
    }
}
