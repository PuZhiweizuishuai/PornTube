package com.buguagaoshu.tiktube.cache;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-10
 * 两步认证登录时保存在服务端的待验证状态。
 * 客户端只拿到一个随机不透明 ID，不再把用户实体（密码哈希、TOTP 密钥、恢复码）加密后下发给浏览器。
 */
@Component
public class TotpLoginChallengeCache {

    /**
     * 同一个待验证会话最多允许尝试的验证码次数
     */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    /**
     * 记录一次待验证登录，返回给客户端的不透明 ID
     */
    public String create(Long userId, boolean rememberMe, long expireTime) {
        long now = System.currentTimeMillis();
        challenges.values().removeIf(challenge -> challenge.expireTime < now);
        String id = randomId();
        challenges.put(id, new Challenge(userId, rememberMe, expireTime));
        return id;
    }

    /**
     * 取出待验证登录并记一次尝试，不存在 / 已过期 / 尝试次数用尽时返回 null
     */
    public Challenge consume(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        Challenge challenge = challenges.get(id);
        if (challenge == null) {
            return null;
        }
        if (challenge.expireTime < System.currentTimeMillis()
                || challenge.attempts.incrementAndGet() > MAX_VERIFY_ATTEMPTS) {
            challenges.remove(id);
            return null;
        }
        return challenge;
    }

    /**
     * 验证成功或明确作废时调用，避免同一个 ID 被重放
     */
    public void invalidate(String id) {
        if (id != null) {
            challenges.remove(id);
        }
    }

    private String randomId() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static class Challenge {
        private final Long userId;
        private final boolean rememberMe;
        private final long expireTime;
        private final AtomicInteger attempts = new AtomicInteger();

        private Challenge(Long userId, boolean rememberMe, long expireTime) {
            this.userId = userId;
            this.rememberMe = rememberMe;
            this.expireTime = expireTime;
        }

        public Long getUserId() {
            return userId;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }
    }
}
