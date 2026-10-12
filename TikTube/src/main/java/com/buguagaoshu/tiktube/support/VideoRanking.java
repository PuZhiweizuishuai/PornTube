package com.buguagaoshu.tiktube.support;

import com.buguagaoshu.tiktube.entity.ArticleEntity;

import java.util.function.ToDoubleFunction;

/**
 * 视频排序打分内核
 *
 * 热度与相似度的权重全部集中在这里，Mapper 里的热度表达式必须与本类保持一致
 * （只改一边会让数据库召回的顺序和 Java 精排的顺序互相打架）。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
public final class VideoRanking {

    public static final long ONE_HOUR_MILLIS = 3_600_000L;

    /**
     * 播放是成本最低的信号，权重压到 0.5；收藏/评论代表更强的认可，权重更高
     */
    private static final double VIEW_WEIGHT = 0.5;
    private static final double LIKE_WEIGHT = 3;
    private static final double FAVORITE_WEIGHT = 6;
    private static final double COMMENT_WEIGHT = 4;
    private static final double DANMAKU_WEIGHT = 1.5;

    /**
     * 不喜欢按 8 倍惩罚
     */
    private static final double DISLIKE_WEIGHT = 8;

    /**
     * 质量率下限：新稿件没有点赞点踩时质量率约 0.5，加下限避免把零互动的稿件压成 0 分
     */
    private static final double QUALITY_FLOOR = 0.25;

    /**
     * 时间衰减（Hacker News / Reddit 的 gravity 做法）：热度按发布年龄幂律下降，
     * 偏移 2 小时避免刚发布时分数爆炸
     */
    private static final double AGE_OFFSET_HOURS = 2;
    private static final double GRAVITY = 1.15;

    private static final double SIMILARITY_WEIGHT = 3;
    private static final double TREND_WEIGHT = 1;
    private static final double QUALITY_WEIGHT = 0.6;
    private static final double SAME_CATEGORY_BONUS = 0.4;

    private VideoRanking() {
    }

    /**
     * 还没落库的缓存计数增量（播放、点赞、收藏、评论、弹幕、不喜欢）
     */
    public record PendingCounts(long views, long likes, long favorites,
                                long comments, long danmaku, long dislikes) {

        public static final PendingCounts NONE = new PendingCounts(0, 0, 0, 0, 0, 0);
    }

    /**
     * 互动量，未做时间衰减
     */
    public static double interactions(ArticleEntity article) {
        return interactions(article, PendingCounts.NONE);
    }

    private static double interactions(ArticleEntity article, PendingCounts pending) {
        return (nz(article.getViewCount()) + pending.views()) * VIEW_WEIGHT
                + (nz(article.getLikeCount()) + pending.likes()) * LIKE_WEIGHT
                + (nz(article.getFavoriteCount()) + pending.favorites()) * FAVORITE_WEIGHT
                + (nz(article.getCommentCount()) + pending.comments()) * COMMENT_WEIGHT
                + (nz(article.getDanmakuCount()) + pending.danmaku()) * DANMAKU_WEIGHT
                - (nz(article.getDislikeCount()) + pending.dislikes()) * DISLIKE_WEIGHT;
    }

    /**
     * Laplace 平滑的点赞率，样本越少越靠近 0.5，
     * 避免「1 赞 0 踩」排在「1 万赞 500 踩」前面
     */
    public static double qualityRate(long likeCount, long dislikeCount) {
        return (likeCount + 1.0) / (likeCount + dislikeCount + 2.0);
    }

    public static double qualityRate(ArticleEntity article) {
        return qualityRate(nz(article.getLikeCount()), nz(article.getDislikeCount()));
    }

    /**
     * 热度分 = 互动量 × 质量率 ÷ 发布年龄的幂律衰减。
     * 年龄取 create_time 而不是 update_time：编辑稿件或重新审核不应该让老视频重新上榜
     */
    public static double trendScore(ArticleEntity article, long now) {
        return trendScore(article, PendingCounts.NONE, now);
    }

    /**
     * 叠加缓存增量后的热度分，用于把刚产生、还没同步回数据库的互动算进榜单
     */
    public static double trendScore(ArticleEntity article, PendingCounts pending, long now) {
        double ageHours = Math.max(0, (now - nz(article.getCreateTime())) / (double) ONE_HOUR_MILLIS);
        long likeCount = nz(article.getLikeCount()) + pending.likes();
        long dislikeCount = nz(article.getDislikeCount()) + pending.dislikes();
        return interactions(article, pending)
                * (QUALITY_FLOOR + qualityRate(likeCount, dislikeCount))
                / Math.pow(ageHours + AGE_OFFSET_HOURS, GRAVITY);
    }

    /**
     * 把热度分压到 0~1，取对数抑制头部视频与长尾视频之间的量级差
     */
    public static double normalizeTrend(double score, double maxScore) {
        if (maxScore <= 0 || score <= 0) {
            return 0;
        }
        return Math.min(1, Math.log1p(score) / Math.log1p(maxScore));
    }

    /**
     * 逆文档频率：越冷门的标签重合越能说明两期视频相关
     */
    public static double idf(long totalDocuments, long documentFrequency) {
        return Math.log(1.0 + totalDocuments / (double) (documentFrequency + 1));
    }

    /**
     * 两组标签在 IDF 权重下的余弦相似度，取值 0~1。
     * 传入的标签必须已经去重，否则模长与点积口径不一致
     */
    public static double tagCosine(String[] left, String[] right, ToDoubleFunction<String> weightOf) {
        if (left.length == 0 || right.length == 0) {
            return 0;
        }
        double dot = 0;
        double leftNormSquared = 0;
        for (String tag : left) {
            double weight = weightOf.applyAsDouble(tag);
            leftNormSquared += weight * weight;
            for (String other : right) {
                if (other.equals(tag)) {
                    dot += weight * weight;
                    break;
                }
            }
        }
        double rightNormSquared = 0;
        for (String tag : right) {
            double weight = weightOf.applyAsDouble(tag);
            rightNormSquared += weight * weight;
        }
        if (leftNormSquared <= 0 || rightNormSquared <= 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNormSquared) * Math.sqrt(rightNormSquared));
    }

    /**
     * 相关推荐综合分：标签相似度主导，热度、口碑、同分区作为次级信号
     */
    public static double recommendScore(double similarity, double normalizedTrend, double qualityRate,
                                        boolean sameCategory) {
        return SIMILARITY_WEIGHT * similarity
                + TREND_WEIGHT * normalizedTrend
                + QUALITY_WEIGHT * qualityRate
                + (sameCategory ? SAME_CATEGORY_BONUS : 0);
    }

    private static long nz(Long value) {
        return value == null ? 0 : value;
    }
}
