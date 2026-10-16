package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.cache.CountRecorder.CountFamily;
import com.buguagaoshu.tiktube.entity.ArticleEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一批稿件的未落库计数增量快照
 *
 * 背景：列表接口原先对每篇稿件逐个读取 6 个计数（点赞、收藏、评论、点踩、弹幕 5 个
 * {@link CountRecorder} 族，加上 {@link PlayCountRecorder} 的播放增量），
 * 一页 50 条就是 300 次读取，Redis 模式下等于 300 次串行网络往返，
 * 而且这些阻塞 IO 原先还跑在 parallelStream 的公共 ForkJoinPool 上。
 * 这里改成整页只发 6 次批量读取，之后按稿件 ID 在内存里取用。
 *
 * 快照是不可变的，构造完成后计数缓冲再怎么变都不影响已取到的这份数据。
 */
public final class ArticleCountSnapshot {

    private static final ArticleCountSnapshot EMPTY =
            new ArticleCountSnapshot(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

    private final Map<Long, Long> comment;
    private final Map<Long, Long> like;
    private final Map<Long, Long> favorite;
    private final Map<Long, Long> dislike;
    private final Map<Long, Long> danmaku;
    private final Map<Long, Long> play;

    private ArticleCountSnapshot(Map<Long, Long> comment, Map<Long, Long> like, Map<Long, Long> favorite,
                                 Map<Long, Long> dislike, Map<Long, Long> danmaku, Map<Long, Long> play) {
        this.comment = comment;
        this.like = like;
        this.favorite = favorite;
        this.dislike = dislike;
        this.danmaku = danmaku;
        this.play = play;
    }

    /**
     * 读取一批稿件的全部未落库增量，每个计数族一次批量读取
     *
     * @param articleIds 稿件 ID，允许含 null 与重复项
     */
    public static ArticleCountSnapshot load(CountRecorder countRecorder,
                                            PlayCountRecorder playCountRecorder,
                                            Collection<Long> articleIds) {
        List<Long> ids = distinctIds(articleIds);
        if (ids.isEmpty()) {
            return EMPTY;
        }
        return new ArticleCountSnapshot(
                countRecorder.getCounts(CountFamily.ARTICLE_COMMENT, ids),
                countRecorder.getCounts(CountFamily.ARTICLE_LIKE, ids),
                countRecorder.getCounts(CountFamily.ARTICLE_FAVORITE, ids),
                countRecorder.getCounts(CountFamily.ARTICLE_DISLIKE, ids),
                countRecorder.getCounts(CountFamily.DANMAKU, ids),
                playCountRecorder.getPlayCounts(ids));
    }

    /**
     * 把未落库增量叠加到稿件实体上，口径与 {@link CountRecorder#syncArticleCount} 一致，
     * 但不含播放量（播放量叠加在视图对象的 viewCount 上，见 {@link #playCount}）
     */
    public void applyTo(ArticleEntity article) {
        long id = article.getId();
        article.setCommentCount(nz(article.getCommentCount()) + commentCount(id));
        article.setLikeCount(nz(article.getLikeCount()) + likeCount(id));
        article.setFavoriteCount(nz(article.getFavoriteCount()) + favoriteCount(id));
        article.setDislikeCount(nz(article.getDislikeCount()) + dislikeCount(id));
        article.setDanmakuCount(nz(article.getDanmakuCount()) + danmakuCount(id));
    }

    public long commentCount(long articleId) {
        return of(comment, articleId);
    }

    public long likeCount(long articleId) {
        return of(like, articleId);
    }

    public long favoriteCount(long articleId) {
        return of(favorite, articleId);
    }

    public long dislikeCount(long articleId) {
        return of(dislike, articleId);
    }

    public long danmakuCount(long articleId) {
        return of(danmaku, articleId);
    }

    public long playCount(long articleId) {
        return of(play, articleId);
    }

    private static List<Long> distinctIds(Collection<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return List.of();
        }
        Set<Long> distinct = new LinkedHashSet<>(articleIds.size() * 2);
        for (Long id : articleIds) {
            if (id != null) {
                distinct.add(id);
            }
        }
        return new ArrayList<>(distinct);
    }

    private static long of(Map<Long, Long> counts, long articleId) {
        Long value = counts.get(articleId);
        return value == null ? 0 : value;
    }

    private static long nz(Long value) {
        return value == null ? 0 : value;
    }
}
