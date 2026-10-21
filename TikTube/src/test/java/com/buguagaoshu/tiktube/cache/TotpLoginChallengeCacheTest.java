package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpLoginChallengeCacheTest {

    /**
     * 未注入 RedisTemplate 时 isReachable() 为 false，走的是内存存储这一条分支
     */
    private static TotpLoginChallengeCache memoryCache() {
        return new TotpLoginChallengeCache(new RedisRepository(), new MyConfigProperties());
    }

    @Test
    void challengeIsSingleUseAndScopedToUserId() {
        TotpLoginChallengeCache cache = memoryCache();
        String id = cache.create(7L, true, System.currentTimeMillis() + 60000);

        assertNotNull(id);
        assertTrue(id.length() >= 32);

        TotpLoginChallengeCache.Challenge challenge = cache.consume(id);
        assertEquals(7L, challenge.getUserId());
        assertTrue(challenge.isRememberMe());

        cache.invalidate(id);
        assertNull(cache.consume(id));
        assertNull(cache.consume(null));
        assertNull(cache.consume("not-exist"));
    }

    @Test
    void expiredAndOverusedChallengesAreRejected() {
        TotpLoginChallengeCache cache = memoryCache();

        String expired = cache.create(1L, false, System.currentTimeMillis() - 1);
        assertNull(cache.consume(expired));

        String limited = cache.create(2L, false, System.currentTimeMillis() + 60000);
        for (int i = 0; i < 5; i++) {
            assertNotNull(cache.consume(limited));
        }
        assertNull(cache.consume(limited));
    }
}
