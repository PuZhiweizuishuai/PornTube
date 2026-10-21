package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SharedCacheVersionStoreTest {

    private static final SharedCache CACHE = SharedCache.CATEGORY;

    private static SharedCacheVersionStore versionStore(FakeRedisRepository redis) {
        MyConfigProperties properties = new MyConfigProperties();
        properties.setOpenRedis(true);
        return new SharedCacheVersionStore(redis, properties);
    }

    @Test
    void markUpdatedPublishesMonotonicVersions() {
        FakeRedisRepository redis = new FakeRedisRepository();
        SharedCacheVersionStore store = versionStore(redis);

        store.markUpdated(CACHE);
        assertEquals(1L, store.appliedVersion(CACHE));

        store.markUpdated(CACHE);
        assertEquals(2L, store.appliedVersion(CACHE));
        assertEquals(2L, store.remoteVersion(CACHE));

        SharedCacheVersionStore otherInstance = versionStore(redis);
        assertNull(otherInstance.appliedVersion(CACHE));
        assertEquals(2L, otherInstance.remoteVersion(CACHE));
    }

    @Test
    void markUpdatedIsNoOpWhenRedisDisabled() {
        FakeRedisRepository redis = new FakeRedisRepository();
        SharedCacheVersionStore store = new SharedCacheVersionStore(redis, new MyConfigProperties());

        store.markUpdated(CACHE);

        assertNull(store.appliedVersion(CACHE));
        assertNull(store.remoteVersion(CACHE));
    }

    @Test
    void rebuildSuppressesBroadcastAndRecordsVersion() {
        FakeRedisRepository redis = new FakeRedisRepository();
        SharedCacheVersionStore store = versionStore(redis);
        store.markUpdated(CACHE);

        store.rebuild(CACHE, () -> {
            store.markUpdated(CACHE);
            return true;
        }, 5L);

        assertEquals(5L, store.appliedVersion(CACHE));
        assertEquals(1L, store.remoteVersion(CACHE));
    }

    @Test
    void skippedOrFailedRebuildKeepsPreviousVersion() {
        FakeRedisRepository redis = new FakeRedisRepository();
        SharedCacheVersionStore store = versionStore(redis);
        store.markUpdated(CACHE);

        store.rebuild(CACHE, () -> false, 9L);
        assertEquals(1L, store.appliedVersion(CACHE));

        store.rebuild(CACHE, () -> {
            throw new IllegalStateException("database unavailable");
        }, 9L);
        assertEquals(1L, store.appliedVersion(CACHE));
    }

    @Test
    void nonNumericLegacyVersionIsReset() {
        FakeRedisRepository redis = new FakeRedisRepository();
        redis.putRaw(CACHE.versionKey(), "2026-10-19T00:00:00Z");
        SharedCacheVersionStore store = versionStore(redis);

        store.markUpdated(CACHE);

        assertEquals(1L, store.appliedVersion(CACHE));
        assertEquals(1L, store.remoteVersion(CACHE));
    }

    /**
     * 模仿 Redis 的 INCR 语义：键不存在从 delta 开始，键值不是整数时抛错
     */
    private static class FakeRedisRepository extends RedisRepository {

        private final Map<String, Object> store = new HashMap<>();

        void putRaw(String key, Object value) {
            store.put(key, value);
        }

        @Override
        public boolean isReachable() {
            return true;
        }

        @Override
        public long incr(String key, long delta) {
            Object current = store.get(key);
            if (current == null) {
                store.put(key, delta);
                return delta;
            }
            long parsed;
            try {
                parsed = Long.parseLong(current.toString());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("ERR value is not an integer", e);
            }
            long next = parsed + delta;
            store.put(key, next);
            return next;
        }

        @Override
        public Object get(String key) {
            return store.get(key);
        }

        @Override
        public void del(String... keys) {
            for (String key : keys) {
                store.remove(key);
            }
        }

        @Override
        public boolean expire(String key, long time) {
            return true;
        }
    }
}
