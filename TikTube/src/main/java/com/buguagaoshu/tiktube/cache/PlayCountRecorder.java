package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-22
 * 播放量缓存计数器
 * 缓存播放量，定期批量写入数据库，减少数据库读写压力。
 * IP 防重放窗口（playDedupWindowMillis）与刷库间隔解耦：
 * 同一 IP 对同一视频在窗口期内只计一次，无论中间刷了几轮库。
 */
@Component
public class PlayCountRecorder {
    // Redis中存储的键名（IP 去重键仍为每视频一个 Set，计数集中在 play:counts Hash）
    private static final String PLAY_IP_KEY_PREFIX = "play:ip:";

    private final PlayStorage playStorage;

    @Autowired
    public PlayCountRecorder(RedisRepository redisRepository, MyConfigProperties configProperties) {
        // 根据配置决定使用哪种存储实现，Redis 连不上时回退内存
        if (configProperties.getOpenRedis() && redisRepository.isReachable()) {
            this.playStorage = new RedisPlayStorage(redisRepository, configProperties);
        } else {
            this.playStorage = new MemoryPlayStorage(configProperties.getPlayDedupWindowMillis());
        }
    }

    public void recordPlay(Long videoId, String ipAddress) {
        playStorage.recordPlay(videoId, ipAddress);
    }

    /**
     * 只读视图，供展示层叠加未落库的增量，排空语义请用 {@link #drainPlayMap()}
     */
    public Map<Long, Long> getPlayMap() {
        return playStorage.getPlayMap();
    }

    public long getPlayCount(Long videoId) {
        return playStorage.getPlayCount(videoId);
    }

    /**
     * 批量读取多个视频未落库的播放增量，Redis 模式下一次 HMGET 完成。
     * 列表接口用它替代逐条 getPlayCount，返回值只包含有增量的视频
     *
     * @param videoIds 视频 ID，调用方需保证不含 null
     */
    public Map<Long, Long> getPlayCounts(Collection<Long> videoIds) {
        return playStorage.getPlayCounts(videoIds);
    }

    /**
     * 原子取走当前播放增量缓冲，返回的 Map 与后续写入完全隔离
     */
    public Map<Long, Long> drainPlayMap() {
        return playStorage.drainPlayMap();
    }

    /**
     * 落库失败时把快照并回当前缓冲，等待下一轮重试
     */
    public void restorePlayMap(Map<Long, Long> snapshot) {
        playStorage.restorePlayMap(snapshot);
    }

    /** 从计数表里挑出关心的那部分，缺失的 ID 不进结果，读侧按 0 处理 */
    private static Map<Long, Long> pick(Map<Long, Long> source, Collection<Long> ids) {
        Map<Long, Long> result = new HashMap<>(ids.size() * 2);
        for (Long id : ids) {
            Long value = source.get(id);
            if (value != null) {
                result.put(id, value);
            }
        }
        return result;
    }

    /** Redis Hash 的 field 是稿件 ID 的字符串形式 */
    private static List<String> toFields(Collection<Long> ids) {
        List<String> fields = new ArrayList<>(ids.size());
        for (Long id : ids) {
            fields.add(Long.toString(id));
        }
        return fields;
    }

    // 播放存储接口
    private interface PlayStorage {
        void recordPlay(Long videoId, String ipAddress);

        Map<Long, Long> getPlayMap();

        long getPlayCount(Long videoId);

        Map<Long, Long> getPlayCounts(Collection<Long> videoIds);

        Map<Long, Long> drainPlayMap();

        void restorePlayMap(Map<Long, Long> snapshot);
    }

    // 内存存储实现
    private static class MemoryPlayStorage implements PlayStorage {
        private final AtomicReference<ConcurrentMap<Long, Long>> playCounts =
                new AtomicReference<>(new ConcurrentHashMap<>());
        // videoId -> (ip -> 上次计入时间戳)
        private final ConcurrentMap<Long, ConcurrentMap<String, Long>> ipLastPlay = new ConcurrentHashMap<>();
        private final long dedupWindowMillis;

        MemoryPlayStorage(long dedupWindowMillis) {
            this.dedupWindowMillis = dedupWindowMillis;
        }

        @Override
        public void recordPlay(Long videoId, String ipAddress) {
            ConcurrentMap<String, Long> ipTimes =
                    ipLastPlay.computeIfAbsent(videoId, k -> new ConcurrentHashMap<>());
            // 原子判定：窗口内首次出现才计数
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
                playCounts.get().compute(videoId, (k, v) -> v == null ? 1 : v + 1);
            }
        }

        @Override
        public Map<Long, Long> getPlayMap() {
            return playCounts.get();
        }

        @Override
        public long getPlayCount(Long videoId) {
            return playCounts.get().getOrDefault(videoId, 0L);
        }

        @Override
        public Map<Long, Long> getPlayCounts(Collection<Long> videoIds) {
            return pick(playCounts.get(), videoIds);
        }

        @Override
        public Map<Long, Long> drainPlayMap() {
            ConcurrentMap<Long, Long> snapshot = playCounts.getAndSet(new ConcurrentHashMap<>());
            pruneExpiredIpRecords();
            return snapshot;
        }

        @Override
        public void restorePlayMap(Map<Long, Long> snapshot) {
            ConcurrentMap<Long, Long> current = playCounts.get();
            snapshot.forEach((id, count) -> current.compute(id, (k, v) -> v == null ? count : v + count));
        }

        // 刷库时顺带清理过期的 IP 记录，防止去重集合无界增长
        private void pruneExpiredIpRecords() {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<Long, ConcurrentMap<String, Long>>> videos = ipLastPlay.entrySet().iterator();
            while (videos.hasNext()) {
                ConcurrentMap<String, Long> ipTimes = videos.next().getValue();
                ipTimes.values().removeIf(last -> now - last >= dedupWindowMillis);
                if (ipTimes.isEmpty()) {
                    videos.remove();
                }
            }
        }
    }

    // Redis存储实现：播放增量集中在一个 Hash，读写单命令完成，不再 SCAN 全库
    private static class RedisPlayStorage implements PlayStorage {
        private static final String PLAY_COUNT_HASH_KEY = "play:counts";
        private final RedisRepository redisRepository;
        private final long dedupWindowSeconds;

        RedisPlayStorage(RedisRepository redisRepository, MyConfigProperties configProperties) {
            this.redisRepository = redisRepository;
            this.dedupWindowSeconds = Math.max(1, configProperties.getPlayDedupWindowMillis() / 1000);
        }

        @Override
        public void recordPlay(Long videoId, String ipAddress) {
            String ipKey = PLAY_IP_KEY_PREFIX + videoId;
            // SET 添加成功（之前不存在）说明是窗口内首次，才计数并续期
            if (redisRepository.sSet(ipKey, ipAddress) > 0) {
                redisRepository.expire(ipKey, dedupWindowSeconds);
                redisRepository.hIncrBy(PLAY_COUNT_HASH_KEY, Long.toString(videoId), 1);
            }
        }

        @Override
        public Map<Long, Long> getPlayMap() {
            return toIdMap(redisRepository.hGetAllCounts(PLAY_COUNT_HASH_KEY));
        }

        @Override
        public long getPlayCount(Long videoId) {
            return redisRepository.hGetCount(PLAY_COUNT_HASH_KEY, Long.toString(videoId));
        }

        @Override
        public Map<Long, Long> getPlayCounts(Collection<Long> videoIds) {
            return toIdMap(redisRepository.hMultiGetCounts(PLAY_COUNT_HASH_KEY, toFields(videoIds)));
        }

        @Override
        public Map<Long, Long> drainPlayMap() {
            // Lua HGETALL+DEL 原子排空，多实例并发刷库也不会重复计数；
            // IP 去重键保留，靠自身 TTL 过期
            return toIdMap(redisRepository.hDrain(PLAY_COUNT_HASH_KEY));
        }

        @Override
        public void restorePlayMap(Map<Long, Long> snapshot) {
            snapshot.forEach((videoId, count) ->
                    redisRepository.hIncrBy(PLAY_COUNT_HASH_KEY, Long.toString(videoId), count));
        }

        private Map<Long, Long> toIdMap(Map<String, Long> raw) {
            Map<Long, Long> result = new HashMap<>(raw.size());
            raw.forEach((field, count) -> {
                try {
                    result.put(Long.parseLong(field), count);
                } catch (NumberFormatException e) {
                    // 忽略无法解析为整数的 field
                }
            });
            return result;
        }
    }
}
