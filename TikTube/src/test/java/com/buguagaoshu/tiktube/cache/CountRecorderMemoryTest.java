package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内存计数存储的 drain/restore 语义与播放去重窗口测试（不依赖 Redis）
 */
class CountRecorderMemoryTest {

    private MyConfigProperties props() {
        return new MyConfigProperties();
    }

    private CountRecorder countRecorder() {
        return new CountRecorder(null, props());
    }

    @Test
    void drainReturnsAccumulatedDeltasAndIsolatesNewWrites() {
        CountRecorder recorder = countRecorder();
        recorder.recordArticleLike(1L, 3);
        recorder.recordArticleLike(1L, 2);
        recorder.recordArticleLike(2L, 1);

        Map<Long, Long> snapshot = recorder.drain(CountRecorder.CountFamily.ARTICLE_LIKE);
        assertEquals(5L, snapshot.get(1L));
        assertEquals(1L, snapshot.get(2L));

        // drain 之后的新写入进入新缓冲，不影响已取走的快照
        recorder.recordArticleLike(1L, 7);
        assertEquals(5L, snapshot.get(1L));
        assertEquals(7L, recorder.getArticleLikeCount(1L));

        Map<Long, Long> second = recorder.drain(CountRecorder.CountFamily.ARTICLE_LIKE);
        assertEquals(Map.of(1L, 7L), second);
    }

    @Test
    void restoreMergesSnapshotBackIntoCurrentBuffer() {
        CountRecorder recorder = countRecorder();
        recorder.recordCommentLike(9L, 4);
        Map<Long, Long> snapshot = recorder.drain(CountRecorder.CountFamily.COMMENT_LIKE);

        recorder.recordCommentLike(9L, 1);
        recorder.restore(CountRecorder.CountFamily.COMMENT_LIKE, snapshot);
        assertEquals(5L, recorder.getCommentLikeCount(9L));
    }

    @Test
    void supportsNegativeDeltasForUnlike() {
        CountRecorder recorder = countRecorder();
        recorder.recordArticleLike(1L, 3);
        recorder.recordArticleLike(1L, -1);
        assertEquals(2L, recorder.getArticleLikeCount(1L));

        Map<Long, Long> snapshot = recorder.drain(CountRecorder.CountFamily.ARTICLE_LIKE);
        assertEquals(2L, snapshot.get(1L));
    }

    @Test
    void playCountDeduplicatesIpWithinWindow() {
        MyConfigProperties props = props();
        props.setPlayDedupWindowMillis(60_000);
        PlayCountRecorder recorder = new PlayCountRecorder(null, props);

        recorder.recordPlay(1L, "10.0.0.1");
        recorder.recordPlay(1L, "10.0.0.1");
        recorder.recordPlay(1L, "10.0.0.2");
        assertEquals(2L, recorder.getPlayCount(1L));

        Map<Long, Long> snapshot = recorder.drainPlayMap();
        assertEquals(2L, snapshot.get(1L));
        // drain 不清出去重记录：同一 IP 在窗口内不会重复计数
        recorder.recordPlay(1L, "10.0.0.1");
        assertEquals(0L, recorder.getPlayCount(1L));
    }

    @Test
    void playCountRecountsIpAfterWindowExpires() throws InterruptedException {
        MyConfigProperties props = props();
        props.setPlayDedupWindowMillis(30);
        PlayCountRecorder recorder = new PlayCountRecorder(null, props);

        recorder.recordPlay(1L, "10.0.0.1");
        Thread.sleep(60);
        recorder.recordPlay(1L, "10.0.0.1");
        assertEquals(2L, recorder.getPlayCount(1L));
    }

    @Test
    void adsDrainAndRestoreRoundTrip() {
        MyConfigProperties props = props();
        props.setPlayDedupWindowMillis(60_000);
        AdsCountRecorder recorder = new AdsCountRecorder(null, props);

        recorder.recordAds(7, "10.0.0.1");
        recorder.recordAds(7, "10.0.0.1");
        recorder.recordAds(7, "10.0.0.2");

        Map<Integer, Long> snapshot = recorder.drainAdsMap();
        assertEquals(2L, snapshot.get(7));
        assertTrue(recorder.getAdsCount(7) == 0L);

        recorder.restoreAdsMap(snapshot);
        assertEquals(2L, recorder.getAdsCount(7));
    }
}
