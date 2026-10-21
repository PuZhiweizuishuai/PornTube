package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-03
 * 广告浏览量缓存计数器，drain/restore 语义与 {@link PlayCountRecorder} 一致
 */
@Component
public class AdsCountRecorder {

    // Redis中存储的键名（IP 去重键仍为每广告一个 Set，计数集中在 ads:counts Hash）
    private static final String ADS_IP_KEY_PREFIX = "ads:ip:";

    private final AdsStorage adsStorage;

    @Autowired
    public AdsCountRecorder(RedisRepository redisRepository, MyConfigProperties configProperties) {
        // 根据配置决定使用哪种存储实现，Redis 连不上时回退内存
        if (configProperties.getOpenRedis() && redisRepository.isReachable()) {
            this.adsStorage = new RedisAdsStorage(redisRepository, configProperties);
        } else {
            this.adsStorage = new MemoryAdsStorage(configProperties.getPlayDedupWindowMillis());
        }
    }

    public void recordAds(Integer adsId, String ipAddress) {
        adsStorage.recordAds(adsId, ipAddress);
    }

    public long getAdsCount(Integer adsId) {
        return adsStorage.getAdsCount(adsId);
    }

    /**
     * 原子取走当前广告浏览增量缓冲，返回的 Map 与后续写入完全隔离
     */
    public Map<Integer, Long> drainAdsMap() {
        return adsStorage.drainAdsMap();
    }

    /**
     * 落库失败时把快照并回当前缓冲，等待下一轮重试
     */
    public void restoreAdsMap(Map<Integer, Long> snapshot) {
        adsStorage.restoreAdsMap(snapshot);
    }

    // 广告存储接口
    private interface AdsStorage {
        void recordAds(Integer adsId, String ipAddress);

        long getAdsCount(Integer adsId);

        Map<Integer, Long> drainAdsMap();

        void restoreAdsMap(Map<Integer, Long> snapshot);
    }

    // 内存存储实现
    private static class MemoryAdsStorage implements AdsStorage {
        private final AtomicReference<ConcurrentMap<Integer, Long>> adsCounts =
                new AtomicReference<>(new ConcurrentHashMap<>());
        // adsId -> (ip -> 上次计入时间戳)
        private final ConcurrentMap<Integer, ConcurrentMap<String, Long>> ipLastView = new ConcurrentHashMap<>();
        private final long dedupWindowMillis;

        MemoryAdsStorage(long dedupWindowMillis) {
            this.dedupWindowMillis = dedupWindowMillis;
        }

        @Override
        public void recordAds(Integer adsId, String ipAddress) {
            ConcurrentMap<String, Long> ipTimes =
                    ipLastView.computeIfAbsent(adsId, k -> new ConcurrentHashMap<>());
            boolean[] counted = new boolean[1];
            ipTimes.compute(ipAddress, (k, last) -> {
                long now = System.currentTimeMillis();
                if (last == null || now - last >= dedupWindowMillis) {
                    counted[0] = true;
                    return now;
                }
                return last;
            });
            if (counted[0]) {
                adsCounts.get().compute(adsId, (k, v) -> v == null ? 1 : v + 1);
            }
        }

        @Override
        public long getAdsCount(Integer adsId) {
            return adsCounts.get().getOrDefault(adsId, 0L);
        }

        @Override
        public Map<Integer, Long> drainAdsMap() {
            ConcurrentMap<Integer, Long> snapshot = adsCounts.getAndSet(new ConcurrentHashMap<>());
            pruneExpiredIpRecords();
            return snapshot;
        }

        @Override
        public void restoreAdsMap(Map<Integer, Long> snapshot) {
            ConcurrentMap<Integer, Long> current = adsCounts.get();
            snapshot.forEach((id, count) -> current.compute(id, (k, v) -> v == null ? count : v + count));
        }

        private void pruneExpiredIpRecords() {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<Integer, ConcurrentMap<String, Long>>> ads = ipLastView.entrySet().iterator();
            while (ads.hasNext()) {
                ConcurrentMap<String, Long> ipTimes = ads.next().getValue();
                ipTimes.values().removeIf(last -> now - last >= dedupWindowMillis);
                if (ipTimes.isEmpty()) {
                    ads.remove();
                }
            }
        }
    }

    // Redis存储实现：浏览增量集中在一个 Hash，读写单命令完成
    private static class RedisAdsStorage implements AdsStorage {
        private static final String ADS_COUNT_HASH_KEY = "ads:counts";
        private final RedisRepository redisRepository;
        private final long dedupWindowSeconds;

        RedisAdsStorage(RedisRepository redisRepository, MyConfigProperties configProperties) {
            this.redisRepository = redisRepository;
            this.dedupWindowSeconds = Math.max(1, configProperties.getPlayDedupWindowMillis() / 1000);
        }

        @Override
        public void recordAds(Integer adsId, String ipAddress) {
            String ipKey = ADS_IP_KEY_PREFIX + adsId;
            if (redisRepository.sSet(ipKey, ipAddress) > 0) {
                redisRepository.expire(ipKey, dedupWindowSeconds);
                redisRepository.hIncrBy(ADS_COUNT_HASH_KEY, Integer.toString(adsId), 1);
            }
        }

        @Override
        public long getAdsCount(Integer adsId) {
            return redisRepository.hGetCount(ADS_COUNT_HASH_KEY, Integer.toString(adsId));
        }

        @Override
        public Map<Integer, Long> drainAdsMap() {
            // Lua HGETALL+DEL 原子排空，多实例并发刷库也不会重复计数；IP 去重键靠自身 TTL 过期
            Map<Integer, Long> result = new HashMap<>();
            redisRepository.hDrain(ADS_COUNT_HASH_KEY).forEach((field, count) -> {
                try {
                    result.put(Integer.parseInt(field), count);
                } catch (NumberFormatException e) {
                    // 忽略无法解析为整数的 field
                }
            });
            return result;
        }

        @Override
        public void restoreAdsMap(Map<Integer, Long> snapshot) {
            snapshot.forEach((adsId, count) ->
                    redisRepository.hIncrBy(ADS_COUNT_HASH_KEY, Integer.toString(adsId), count));
        }
    }
}
