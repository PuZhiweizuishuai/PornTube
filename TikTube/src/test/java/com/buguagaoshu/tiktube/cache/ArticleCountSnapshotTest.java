package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 批量计数快照测试（内存存储，不依赖 Redis）
 *
 * 列表富化与热门榜精排都改成整批读一次增量，这里保证批量读的结果与逐条读一致，
 * 否则一页稿件的计数会静默算错。
 */
class ArticleCountSnapshotTest {

    private CountRecorder countRecorder() {
        return new CountRecorder(null, new MyConfigProperties());
    }

    private PlayCountRecorder playCountRecorder() {
        return new PlayCountRecorder(null, new MyConfigProperties());
    }

    private ArticleEntity article(long id, long view, long like, long favorite, long comment, long dislike, long danmaku) {
        ArticleEntity article = new ArticleEntity();
        article.setId(id);
        article.setViewCount(view);
        article.setLikeCount(like);
        article.setFavoriteCount(favorite);
        article.setCommentCount(comment);
        article.setDislikeCount(dislike);
        article.setDanmakuCount(danmaku);
        return article;
    }

    @Test
    void batchReadMatchesPerKeyRead() {
        CountRecorder counts = countRecorder();
        PlayCountRecorder plays = playCountRecorder();

        counts.recordArticleComment(1L, 2);
        counts.recordArticleLike(1L, 3);
        counts.recordArticleFavorite(1L, 4);
        counts.recordArticleDislike(1L, 5);
        counts.recordDanmaku(1L, 6L);
        plays.recordPlay(1L, "10.0.0.1");
        counts.recordArticleLike(2L, 7);

        ArticleCountSnapshot snapshot = ArticleCountSnapshot.load(counts, plays, List.of(1L, 2L));

        for (long id : new long[]{1L, 2L}) {
            assertEquals(counts.getArticleCommentCount(id), snapshot.commentCount(id));
            assertEquals(counts.getArticleLikeCount(id), snapshot.likeCount(id));
            assertEquals(counts.getArticleFavoriteCount(id), snapshot.favoriteCount(id));
            assertEquals(counts.getArticleDislikeCount(id), snapshot.dislikeCount(id));
            assertEquals(counts.getDanmakuCount(id), snapshot.danmakuCount(id));
            assertEquals(plays.getPlayCount(id), snapshot.playCount(id));
        }
        assertEquals(3L, snapshot.likeCount(1L));
        assertEquals(7L, snapshot.likeCount(2L));
        assertEquals(1L, snapshot.playCount(1L));
    }

    @Test
    void idWithoutPendingDeltaReadsAsZero() {
        ArticleCountSnapshot snapshot = ArticleCountSnapshot.load(
                countRecorder(), playCountRecorder(), List.of(99L));

        assertEquals(0L, snapshot.likeCount(99L));
        assertEquals(0L, snapshot.playCount(99L));
        assertEquals(0L, snapshot.commentCount(12345L));
    }

    @Test
    void applyToAddsDeltaOnTopOfStoredCount() {
        CountRecorder counts = countRecorder();
        counts.recordArticleLike(1L, 3);
        counts.recordDanmaku(1L, 2L);

        ArticleEntity article = article(1L, 100, 10, 20, 30, 40, 50);
        ArticleCountSnapshot.load(counts, playCountRecorder(), List.of(1L)).applyTo(article);

        assertEquals(13L, article.getLikeCount());
        assertEquals(52L, article.getDanmakuCount());
        // applyTo 不管播放量，viewCount 由视图层单独叠加
        assertEquals(100L, article.getViewCount());
        assertEquals(20L, article.getFavoriteCount());
    }

    @Test
    void toleratesNullAndDuplicateIds() {
        CountRecorder counts = countRecorder();
        counts.recordArticleLike(1L, 3);

        ArticleCountSnapshot snapshot = ArticleCountSnapshot.load(
                counts, playCountRecorder(), Arrays.asList(1L, null, 1L, 2L));

        assertEquals(3L, snapshot.likeCount(1L));
        assertEquals(0L, snapshot.likeCount(2L));
    }

    @Test
    void snapshotIsFrozenAgainstLaterWrites() {
        CountRecorder counts = countRecorder();
        counts.recordArticleLike(1L, 3);

        ArticleCountSnapshot snapshot = ArticleCountSnapshot.load(counts, playCountRecorder(), List.of(1L));
        counts.recordArticleLike(1L, 100);

        assertEquals(3L, snapshot.likeCount(1L));
    }

    @Test
    void emptyIdListNeedsNoStorageRead() {
        ArticleCountSnapshot snapshot = ArticleCountSnapshot.load(
                countRecorder(), playCountRecorder(), List.of());

        assertEquals(0L, snapshot.likeCount(1L));
        assertEquals(0L, snapshot.playCount(1L));
    }
}
