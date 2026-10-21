package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-10
 * 两步认证登录时保存在服务端的待验证状态。
 * 客户端只拿到一个随机不透明 ID，不再把用户实体（密码哈希、TOTP 密钥、恢复码）加密后下发给浏览器。
 * <p>
 * 开启 open-redis 后待验证状态存 Redis，「A 实例发起登录、B 实例提交验证码」也能验通；
 * 关闭时用内存，过期与次数由本进程维护。
 */
@Slf4j
@Component
public class TotpLoginChallengeCache {

    /**
     * 同一个待验证会话最多允许尝试的验证码次数
     */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final String CHALLENGE_KEY_PREFIX = "totp:challenge:";

    private static final String FIELD_USER_ID = "userId";

    private static final String FIELD_REMEMBER_ME = "rememberMe";

    private static final String FIELD_ATTEMPTS = "attempts";

    private final ChallengeStorage challengeStorage;

    @Autowired
    public TotpLoginChallengeCache(RedisRepository redisRepository, MyConfigProperties configProperties) {
        if (configProperties.getOpenRedis() && redisRepository.isReachable()) {
            this.challengeStorage = new RedisChallengeStorage(redisRepository);
        } else {
            this.challengeStorage = new MemoryChallengeStorage();
        }
    }

    /**
     * 记录一次待验证登录，返回给客户端的不透明 ID
     */
    public String create(Long userId, boolean rememberMe, long expireTime) {
        return challengeStorage.create(userId, rememberMe, expireTime);
    }

    /**
     * 取出待验证登录并记一次尝试，不存在 / 已过期 / 尝试次数用尽时返回 null
     */
    public Challenge consume(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return challengeStorage.consume(id);
    }

    /**
     * 验证成功或明确作废时调用，避免同一个 ID 被重放
     */
    public void invalidate(String id) {
        if (id != null && !id.isEmpty()) {
            challengeStorage.invalidate(id);
        }
    }

    /**
     * 待验证登录的不透明 ID，只由随机数决定，与存储方式无关
     */
    private static String randomId() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 交给调用方的待验证登录，不含有效期与尝试次数这些存储细节
     */
    public static class Challenge {
        private final Long userId;
        private final boolean rememberMe;

        private Challenge(Long userId, boolean rememberMe) {
            this.userId = userId;
            this.rememberMe = rememberMe;
        }

        public Long getUserId() {
            return userId;
        }

        public boolean isRememberMe() {
            return rememberMe;
        }
    }

    private interface ChallengeStorage {
        String create(Long userId, boolean rememberMe, long expireTime);

        Challenge consume(String id);

        void invalidate(String id);
    }

    private static class MemoryChallengeStorage implements ChallengeStorage {

        private final Map<String, Entry> challenges = new ConcurrentHashMap<>();

        @Override
        public String create(Long userId, boolean rememberMe, long expireTime) {
            long now = System.currentTimeMillis();
            challenges.values().removeIf(challenge -> challenge.expireTime < now);
            String id = randomId();
            challenges.put(id, new Entry(userId, rememberMe, expireTime));
            return id;
        }

        @Override
        public Challenge consume(String id) {
            Entry entry = challenges.get(id);
            if (entry == null) {
                return null;
            }
            if (entry.expireTime < System.currentTimeMillis()
                    || entry.attempts.incrementAndGet() > MAX_VERIFY_ATTEMPTS) {
                challenges.remove(id);
                return null;
            }
            return new Challenge(entry.userId, entry.rememberMe);
        }

        @Override
        public void invalidate(String id) {
            challenges.remove(id);
        }

        private static class Entry {
            private final Long userId;
            private final boolean rememberMe;
            private final long expireTime;
            private final AtomicInteger attempts = new AtomicInteger();

            private Entry(Long userId, boolean rememberMe, long expireTime) {
                this.userId = userId;
                this.rememberMe = rememberMe;
                this.expireTime = expireTime;
            }
        }
    }

    private static class RedisChallengeStorage implements ChallengeStorage {

        private final RedisRepository redisRepository;

        private RedisChallengeStorage(RedisRepository redisRepository) {
            this.redisRepository = redisRepository;
        }

        @Override
        public String create(Long userId, boolean rememberMe, long expireTime) {
            String id = randomId();
            String key = challengeKey(id);
            Map<String, Object> fields = new HashMap<>(4);
            fields.put(FIELD_USER_ID, Long.toString(userId));
            fields.put(FIELD_REMEMBER_ME, Boolean.toString(rememberMe));
            // 过期时间由 Redis TTL 维护，不需要再像内存实现那样遍历清理；
            // 多个字段与 TTL 在 MULTI 里一次写完，避免半条记录被别的实例读到。
            // 写失败必须直接抛出去：返回一个 id 会让用户拿着注定验不过的凭证反复重试，
            // 而界面上只会看到「登录信息已过期」，和真实原因完全对不上
            if (!redisRepository.replaceHashWithTtl(key, fields, ttlSeconds(expireTime))) {
                throw new IllegalStateException("两步验证服务暂不可用，请稍后重试（待验证状态写入失败）");
            }
            return id;
        }

        @Override
        public Challenge consume(String id) {
            try {
                Object rawUserId = redisRepository.hget(challengeKey(id), FIELD_USER_ID);
                if (rawUserId == null) {
                    return null;
                }
                if (usedUpAttempts(id)) {
                    invalidate(id);
                    return null;
                }
                Object rawRememberMe = redisRepository.hget(challengeKey(id), FIELD_REMEMBER_ME);
                return new Challenge(Long.parseLong(rawUserId.toString()),
                        rawRememberMe != null && Boolean.parseBoolean(rawRememberMe.toString()));
            } catch (Exception e) {
                // 读不到就当待验证登录失效，让用户回到登录页重来，不要让登录流程因 Redis 抖动报 500
                log.error("读取 TOTP 待验证登录状态失败", e);
                return null;
            }
        }

        @Override
        public void invalidate(String id) {
            try {
                redisRepository.del(challengeKey(id));
            } catch (Exception e) {
                // TTL 会自动清掉，清理失败不影响本次验证结果
                log.warn("删除 TOTP 待验证登录状态失败：{}", e.toString());
            }
        }

        /**
         * 尝试次数记在待验证状态同一个 Hash 里，跟着同一个 TTL 过期，不用维护第二个键
         */
        private boolean usedUpAttempts(String id) {
            return redisRepository.hIncrBy(challengeKey(id), FIELD_ATTEMPTS, 1L) > MAX_VERIFY_ATTEMPTS;
        }

        private static long ttlSeconds(long expireTime) {
            long millis = expireTime - System.currentTimeMillis();
            if (millis <= 0) {
                return 1;
            }
            // 向上取整，避免 999ms 的会话被截成 0 秒后立刻过期
            return Math.max(1, (millis + 999) / 1000);
        }

        private static String challengeKey(String id) {
            return CHALLENGE_KEY_PREFIX + id;
        }
    }
}
