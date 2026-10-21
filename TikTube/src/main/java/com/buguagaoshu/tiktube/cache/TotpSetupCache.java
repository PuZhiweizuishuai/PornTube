package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-15
 * 开启 / 重新生成 / 恢复两步验证时保存在服务端的待确认密钥。
 * 只有用户扫码后用验证码确认（/totp/check 验证通过）才会落库生效；
 * 弹窗被关闭或超时未确认时，用户原有的两步验证状态不受任何影响。
 * <p>
 * 开启 open-redis 后密钥与尝试次数都存 Redis，多实例部署时「A 实例发起、B 实例确认」不会丢密钥；
 * 关闭时用内存，过期与次数由本进程维护。
 */
@Slf4j
@Component
public class TotpSetupCache {

    /**
     * 待确认密钥有效期：10 分钟内必须完成扫码 + 验证码确认
     */
    private static final long TTL_MILLIS = 600_000L;

    /**
     * 同一个待确认密钥最多允许尝试的验证码次数
     */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final String PENDING_KEY_PREFIX = "totp:setup:";

    private static final String FIELD_SECRET = "secret";

    private static final String FIELD_RECOVERY_CODE = "recoveryCode";

    private static final String FIELD_ATTEMPTS = "attempts";

    private final PendingStorage pendingStorage;

    @Autowired
    public TotpSetupCache(RedisRepository redisRepository, MyConfigProperties configProperties) {
        if (configProperties.getOpenRedis() && redisRepository.isReachable()) {
            this.pendingStorage = new RedisPendingStorage(redisRepository);
        } else {
            this.pendingStorage = new MemoryPendingStorage();
        }
    }

    /**
     * 记录（或覆盖）某个用户的待确认密钥
     */
    public void put(Long userId, String secret, String recoveryCode) {
        pendingStorage.put(userId, secret, recoveryCode);
    }

    /**
     * 取出待确认密钥并记一次验证尝试，不存在 / 已过期 / 尝试次数用尽时返回 null
     */
    public Pending take(Long userId) {
        if (userId == null) {
            return null;
        }
        return pendingStorage.take(userId);
    }

    /**
     * 确认成功或用户取消设置时调用，避免同一个待确认密钥被重复使用
     */
    public void invalidate(Long userId) {
        if (userId != null) {
            pendingStorage.invalidate(userId);
        }
    }

    /**
     * 交给调用方的待确认密钥，不含有效期与尝试次数这些存储细节
     */
    public static class Pending {
        private final String secret;
        private final String recoveryCode;

        private Pending(String secret, String recoveryCode) {
            this.secret = secret;
            this.recoveryCode = recoveryCode;
        }

        public String getSecret() {
            return secret;
        }

        public String getRecoveryCode() {
            return recoveryCode;
        }
    }

    private interface PendingStorage {
        void put(Long userId, String secret, String recoveryCode);

        Pending take(Long userId);

        void invalidate(Long userId);
    }

    private static class MemoryPendingStorage implements PendingStorage {

        private final Map<Long, Entry> pendings = new ConcurrentHashMap<>();

        @Override
        public void put(Long userId, String secret, String recoveryCode) {
            pendings.put(userId, new Entry(secret, recoveryCode, System.currentTimeMillis() + TTL_MILLIS));
        }

        @Override
        public Pending take(Long userId) {
            Entry entry = pendings.get(userId);
            if (entry == null) {
                return null;
            }
            if (entry.expireTime < System.currentTimeMillis()
                    || entry.attempts.incrementAndGet() > MAX_VERIFY_ATTEMPTS) {
                pendings.remove(userId);
                return null;
            }
            return new Pending(entry.secret, entry.recoveryCode);
        }

        @Override
        public void invalidate(Long userId) {
            pendings.remove(userId);
        }

        private static class Entry {
            private final String secret;
            private final String recoveryCode;
            private final long expireTime;
            private final AtomicInteger attempts = new AtomicInteger();

            private Entry(String secret, String recoveryCode, long expireTime) {
                this.secret = secret;
                this.recoveryCode = recoveryCode;
                this.expireTime = expireTime;
            }
        }
    }

    private static class RedisPendingStorage implements PendingStorage {

        private static final long TTL_SECONDS = TTL_MILLIS / 1000;

        private final RedisRepository redisRepository;

        private RedisPendingStorage(RedisRepository redisRepository) {
            this.redisRepository = redisRepository;
        }

        @Override
        public void put(Long userId, String secret, String recoveryCode) {
            String key = pendingKey(userId);
            Map<String, Object> fields = new HashMap<>(4);
            fields.put(FIELD_SECRET, secret);
            fields.put(FIELD_RECOVERY_CODE, recoveryCode);
            // 整键覆盖：重新下发的密钥才能拿到全新的验证次数额度；MULTI 保证不会出现只有一半字段的中间态。
            // 写失败必须抛出去，否则用户扫的是一个服务端根本没存下来的密钥，验证时只会报「验证码错误」
            if (!redisRepository.replaceHashWithTtl(key, fields, TTL_SECONDS)) {
                throw new IllegalStateException("两步验证设置暂不可用，请稍后重试（待确认密钥写入失败）");
            }
        }

        @Override
        public Pending take(Long userId) {
            try {
                String key = pendingKey(userId);
                Object secret = redisRepository.hget(key, FIELD_SECRET);
                if (secret == null) {
                    return null;
                }
                if (usedUpAttempts(key)) {
                    invalidate(userId);
                    return null;
                }
                Object recoveryCode = redisRepository.hget(key, FIELD_RECOVERY_CODE);
                return new Pending(secret.toString(), recoveryCode == null ? null : recoveryCode.toString());
            } catch (Exception e) {
                // 读不到就当没有待确认密钥，让用户重新发起设置，不要让登录/设置流程因 Redis 抖动报 500
                log.error("读取用户 {} 的待确认 TOTP 密钥失败", userId, e);
                return null;
            }
        }

        @Override
        public void invalidate(Long userId) {
            try {
                redisRepository.del(pendingKey(userId));
            } catch (Exception e) {
                // TTL 会自动清掉，清理失败不影响本次确认结果
                log.warn("删除用户 {} 的待确认 TOTP 密钥失败：{}", userId, e.toString());
            }
        }

        /**
         * 尝试次数记在待确认密钥同一个 Hash 里，跟着同一个 TTL 过期，多实例提交验证码都算得到
         */
        private boolean usedUpAttempts(String key) {
            return redisRepository.hIncrBy(key, FIELD_ATTEMPTS, 1L) > MAX_VERIFY_ATTEMPTS;
        }

        private static String pendingKey(Long userId) {
            return PENDING_KEY_PREFIX + userId;
        }
    }
}
