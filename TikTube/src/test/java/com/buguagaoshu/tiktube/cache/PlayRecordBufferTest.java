package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 播放记录缓冲的合并/drain/restore 语义测试（不依赖数据库）
 */
class PlayRecordBufferTest {

    private PlayRecordingEntity onlyRecord(PlayRecordBuffer buffer) {
        List<PlayRecordingEntity> drained = buffer.drain();
        assertEquals(1, drained.size());
        return drained.get(0);
    }

    @Test
    void sameKeyMergesIntoOneRowKeepingFirstCreateTime() throws InterruptedException {
        PlayRecordBuffer buffer = new PlayRecordBuffer();
        long before = System.currentTimeMillis();
        buffer.record(10L, 100L, 1L, 30.0, "ua-old");
        Thread.sleep(20);
        buffer.record(10L, 100L, 1L, 60.0, "ua-new");

        PlayRecordingEntity merged = onlyRecord(buffer);
        // create_time 取本周期首次上报，update_time 取最后一次
        assertTrue(merged.getCreateTime() >= before);
        assertTrue(merged.getCreateTime() < merged.getUpdateTime());
        assertEquals(60.0, merged.getVideoTime().doubleValue());
        assertEquals("ua-new", merged.getUa());
    }

    @Test
    void nullVideoTimeDoesNotEraseBufferedProgress() {
        PlayRecordBuffer buffer = new PlayRecordBuffer();
        // 进度上报带秒数，字节流鉴权路径只刷新观看时间
        buffer.record(10L, 100L, 1L, 125.5, "ua");
        buffer.record(10L, 100L, 1L, null, "ua");
        assertEquals(125.5, onlyRecord(buffer).getVideoTime().doubleValue());

        buffer.record(10L, 100L, 1L, null, "ua");
        buffer.record(10L, 100L, 1L, 140.0, "ua");
        assertEquals(140.0, onlyRecord(buffer).getVideoTime().doubleValue());
    }

    @Test
    void differentKeysAreNotMerged() {
        PlayRecordBuffer buffer = new PlayRecordBuffer();
        buffer.record(10L, 100L, 1L, 1.0, "ua");
        buffer.record(10L, 101L, 1L, 1.0, "ua");
        buffer.record(10L, 100L, 2L, 1.0, "ua");
        assertEquals(3, buffer.drain().size());
    }

    @Test
    void drainIsolatesNewWritesAndRestoreKeepsNewerReport() {
        PlayRecordBuffer buffer = new PlayRecordBuffer();
        buffer.record(10L, 100L, 1L, 10.0, "ua");
        List<PlayRecordingEntity> snapshot = buffer.drain();
        assertEquals(10.0, snapshot.get(0).getVideoTime().doubleValue());

        // drain 之后的上报进入新缓冲，落库失败并回时以新上报为准
        buffer.record(10L, 100L, 1L, 20.0, "ua");
        buffer.restore(snapshot);
        assertEquals(20.0, onlyRecord(buffer).getVideoTime().doubleValue());

        // 缓冲已被取走时，并回的快照重新生效
        buffer.restore(snapshot);
        assertEquals(10.0, onlyRecord(buffer).getVideoTime().doubleValue());
        assertTrue(buffer.drain().isEmpty());
    }

    @Test
    void bufferedRowCarriesNoFileId() {
        PlayRecordBuffer buffer = new PlayRecordBuffer();
        buffer.record(10L, 100L, 1L, 10.0, "ua");
        assertNull(onlyRecord(buffer).getFileId());
    }
}
