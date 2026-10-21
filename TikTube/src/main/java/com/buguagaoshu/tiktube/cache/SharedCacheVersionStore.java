package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-19
 * 快照缓存的版本协调：写入方刷完本地快照就把版本号发到 Redis，其他实例轮询到新版本就回源重建。
 * <p>
 * 版本号用 Redis 端自增计数，不用各实例的本地时间戳：多实例时钟有偏差时，
 * 时间戳会出现「后发生的写入版本号反而更小」，导致部分实例永远不重建。
 * <p>
 * 没开 open-redis 时这里所有方法都是空操作，各缓存只用自己的内存快照，行为跟改造前一致。
 */
@Slf4j
@Component
public class SharedCacheVersionStore {

    /**
     * 版本号只需要活过轮询间隔。过期后下一次写入从 1 重新开始，
     * 各实例发现版本不等于本地记录的值会再重建一次，不会因为计数器归零而卡死
     */
    private static final long VERSION_TTL_SECONDS = 86_400L;

    private final RedisRepository redisRepository;

    private final MyConfigProperties configProperties;

    /**
     * 本实例已经应用到哪一版，避免同一版本反复重建
     */
    private final Map<SharedCache, Long> applied = new ConcurrentHashMap<>();

    /**
     * 当前线程正在为哪个缓存回源重建。重建本身会调用写入方那些会广播的方法，
     * 靠这个标记压掉，否则 A 重建 → 广播 → B 重建 → 广播 → A 又重建，实例之间来回打转
     */
    private final ThreadLocal<SharedCache> rebuilding = new ThreadLocal<>();

    @Autowired
    public SharedCacheVersionStore(RedisRepository redisRepository, MyConfigProperties configProperties) {
        this.redisRepository = redisRepository;
        this.configProperties = configProperties;
    }

    /**
     * 是否已经开启 Redis 共享，未开启或 Redis 连不上时不做任何跨实例同步
     */
    public boolean isShared() {
        return configProperties.getOpenRedis() && redisRepository.isReachable();
    }

    /**
     * 写入方刷新完本地快照后调用，把新版本广播给其他实例
     */
    public void markUpdated(SharedCache cache) {
        if (isSuppressed(cache) || !isShared()) {
            return;
        }
        String key = cache.versionKey();
        long version;
        try {
            version = redisRepository.incr(key, 1L);
        } catch (Exception e) {
            // 历史上这里存过时间戳，若键值不是整数，删掉后从 1 重新计数；连接错误会在重试里再次失败
            try {
                redisRepository.del(key);
                version = redisRepository.incr(key, 1L);
            } catch (Exception retry) {
                log.warn("广播缓存 {} 的版本失败，其他实例可能继续使用旧快照", cache, retry);
                return;
            }
        }
        redisRepository.expire(key, VERSION_TTL_SECONDS);
        applied.put(cache, version);
    }

    /**
     * Redis 上的当前版本，没写过则返回 null
     */
    public Long remoteVersion(SharedCache cache) {
        Object raw = redisRepository.get(cache.versionKey());
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            log.warn("缓存 {} 的版本号读出来不是数字：{}，按无版本处理", cache, raw);
            return null;
        }
    }

    /**
     * 本实例已应用的版本，从没应用过则返回 null
     */
    public Long appliedVersion(SharedCache cache) {
        return applied.get(cache);
    }

    /**
     * 回源重建本实例的这一份快照，期间抑制广播，成功后才记下版本，失败或被跳过则保留上一份等下一轮重试
     */
    public void rebuild(SharedCache cache, BooleanSupplier reloader, long version) {
        rebuilding.set(cache);
        try {
            if (reloader.getAsBoolean()) {
                applied.put(cache, version);
            } else {
                log.warn("缓存 {} 的重建被跳过，下一轮同步继续尝试", cache);
            }
        } catch (Exception e) {
            log.error("重建 {} 缓存失败，继续使用上一份快照", cache, e);
        } finally {
            rebuilding.remove();
        }
    }

    private boolean isSuppressed(SharedCache cache) {
        return rebuilding.get() == cache;
    }
}
