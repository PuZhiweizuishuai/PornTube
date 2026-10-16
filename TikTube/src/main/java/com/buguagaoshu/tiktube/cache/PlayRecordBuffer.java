package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-16
 * 播放记录写缓冲
 * <p>
 * 播放鉴权挂在视频字节流接口上，一次观看会发出多个 Range 请求，原先每个请求都要同步
 * SELECT + 写库，所有观看请求还会在同一把全局锁上排队。这里把写入按 (用户, 稿件, 分P)
 * 合并进内存，只保留每个键的最新一次上报，由定时任务批量 upsert，请求线程不再触碰数据库。
 * <p>
 * 与计数缓冲不同，这里缓冲的是整行而非增量，且落库语句是幂等 upsert，多实例各写各的
 * 由 play_recording 的唯一索引收敛，不需要 Redis 中转。
 */
@Component
public class PlayRecordBuffer {

    private record PlayKey(Long userId, Long articleId, Long videoId) {
    }

    private final AtomicReference<ConcurrentMap<PlayKey, PlayRecordingEntity>> pending =
            new AtomicReference<>(new ConcurrentHashMap<>());

    /**
     * 记录一次观看
     *
     * @param videoTime 观看进度，为 null 表示只刷新观看时间，不覆盖已缓冲的进度
     */
    public void record(Long articleId, Long videoId, Long userId, Double videoTime, String ua) {
        long now = System.currentTimeMillis();
        PlayRecordingEntity latest = new PlayRecordingEntity();
        latest.setArticleId(articleId);
        latest.setVideoId(videoId);
        latest.setUserId(userId);
        latest.setVideoTime(videoTime);
        latest.setUa(ua);
        latest.setCreateTime(now);
        latest.setUpdateTime(now);

        pending.get().compute(new PlayKey(userId, articleId, videoId), (key, buffered) -> {
            if (buffered == null) {
                return latest;
            }
            // create_time 用本周期首次记录的时间，落库时已存在的行由 SQL 保持原值
            latest.setCreateTime(buffered.getCreateTime());
            if (videoTime == null) {
                latest.setVideoTime(buffered.getVideoTime());
            }
            return latest;
        });
    }

    /**
     * 原子取走当前待写缓冲，返回的行与后续写入完全隔离。
     * 换表瞬间落入旧表的少量写入会错过本轮，由该用户的下一次播放上报补回，因此不加锁
     */
    public List<PlayRecordingEntity> drain() {
        ConcurrentMap<PlayKey, PlayRecordingEntity> snapshot = pending.getAndSet(new ConcurrentHashMap<>());
        return new ArrayList<>(snapshot.values());
    }

    /**
     * 落库失败时把快照并回当前缓冲，已有更新上报的键以新上报为准，等待下一轮重试
     */
    public void restore(List<PlayRecordingEntity> snapshot) {
        ConcurrentMap<PlayKey, PlayRecordingEntity> current = pending.get();
        for (PlayRecordingEntity playRecording : snapshot) {
            current.putIfAbsent(new PlayKey(playRecording.getUserId(),
                    playRecording.getArticleId(), playRecording.getVideoId()), playRecording);
        }
    }
}
