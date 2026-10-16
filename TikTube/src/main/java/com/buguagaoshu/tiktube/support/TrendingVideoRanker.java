package com.buguagaoshu.tiktube.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buguagaoshu.tiktube.cache.ArticleCountSnapshot;
import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.PlayCountRecorder;
import com.buguagaoshu.tiktube.dao.ArticleDao;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 热门榜计算
 *
 * 背景：hotView 原先住在 ArticleServiceImpl 里，但它跟稿件的增删改查没有任何关系——
 * 定时任务 {@code HotTasks} 与管理员手动刷新各自调一次，结果整份塞进 HotCache，
 * 请求路径只读缓存。这里单独拆出来，ArticleServiceImpl 只保留一行委托。
 *
 * 两阶段召回：数据库按衰减热度公式在时间窗内粗排，Java 侧叠加还没落库的计数增量精排。
 * 两阶段用的是同一套公式（{@link VideoRanking} 与 ArticleDao.xml 里的表达式必须保持一致）。
 */
@Slf4j
@Component
public class TrendingVideoRanker {

    private static final long ONE_DAY_MILLIS = 86_400_000L;

    /**
     * 热门榜的时间窗：只在这个窗口里召回候选，配合 create_time 索引避免每次刷新扫全库
     */
    private static final long TRENDING_WINDOW_MILLIS = 30 * ONE_DAY_MILLIS;

    /**
     * 一阶段多召回多少候选留给精排
     */
    private static final int TREND_RECALL_FACTOR = 2;

    /**
     * 播放增量键数量超过这个值就不再用它补候选，避免扫描过多 Redis 键
     */
    private static final int MAX_PENDING_PLAY_KEYS = 5000;

    private final ArticleDao articleDao;
    private final CountRecorder countRecorder;
    private final PlayCountRecorder playCountRecorder;
    private final ArticleViewEnricher articleViewEnricher;
    private final ArticleVisibilityResolver visibilityResolver;

    @Autowired
    public TrendingVideoRanker(ArticleDao articleDao,
                               CountRecorder countRecorder,
                               PlayCountRecorder playCountRecorder,
                               ArticleViewEnricher articleViewEnricher,
                               ArticleVisibilityResolver visibilityResolver) {
        this.articleDao = articleDao;
        this.countRecorder = countRecorder;
        this.playCountRecorder = playCountRecorder;
        this.articleViewEnricher = articleViewEnricher;
        this.visibilityResolver = visibilityResolver;
    }

    /**
     * 算一份热门榜
     *
     * @param num 榜单长度，非正数返回空列表
     */
    public List<ArticleViewData> rank(int num) {
        if (num <= 0) {
            return new ArrayList<>();
        }
        long now = System.currentTimeMillis();
        int videoType = FileTypeEnum.VIDEO.getCode();

        // 一阶段：数据库按衰减热度在窗口内召回候选，多取一倍留给精排
        List<ArticleEntity> candidates = new ArrayList<>(articleDao.findTrendingVideos(
                now, now - TRENDING_WINDOW_MILLIS, num * TREND_RECALL_FACTOR, videoType));
        if (candidates.size() < num) {
            // 站内稿件少的时候窗口内凑不满榜单，退化成不带时间窗的同公式查询
            candidates = new ArrayList<>(articleDao.findTrendingVideos(
                    now, null, num * TREND_RECALL_FACTOR, videoType));
        } else {
            // 带时间窗的召回只看得见新稿件，老稿件今天突然火起来的情况靠播放增量补一路候选
            candidates.addAll(recentSurgeCandidates(num, videoType));
        }

        Map<Long, ArticleEntity> uniqueCandidates = new LinkedHashMap<>();
        for (ArticleEntity candidate : candidates) {
            if (candidate.getId() != null) {
                uniqueCandidates.putIfAbsent(candidate.getId(), candidate);
            }
        }
        if (uniqueCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 二阶段：叠加还没同步回数据库的计数增量，用同一套公式精排；整批只发 6 次批量读取
        ArticleCountSnapshot counts = ArticleCountSnapshot.load(
                countRecorder, playCountRecorder, uniqueCandidates.keySet());
        List<ArticleEntity> ranked = new ArrayList<>(uniqueCandidates.values());
        for (ArticleEntity candidate : ranked) {
            candidate.setSort(liveTrendScore(candidate, counts, now));
        }
        ranked.sort(Comparator.comparingDouble(ArticleEntity::getSort).reversed());

        return articleViewEnricher.enrich(ranked.size() > num ? ranked.subList(0, num) : ranked);
    }

    /**
     * 播放增量缓存里存的是「上一次落库以来」的播放数，正是当下最活跃的信号，
     * 取增量最多的一批稿件作为第二路候选
     */
    private List<ArticleEntity> recentSurgeCandidates(int limit, int videoType) {
        Map<Long, Long> pendingPlays = playCountRecorder.getPlayMap();
        if (pendingPlays.isEmpty() || pendingPlays.size() > MAX_PENDING_PLAY_KEYS) {
            return List.of();
        }
        List<Long> hottestIds = pendingPlays.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
        if (hottestIds.isEmpty()) {
            return List.of();
        }

        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", hottestIds);
        List<ArticleEntity> surge = new ArrayList<>(hottestIds.size());
        for (ArticleEntity video : articleDao.selectList(wrapper)) {
            if (visibilityResolver.isPubliclyListed(video, videoType)) {
                surge.add(video);
            }
        }
        return surge;
    }

    /**
     * 叠加缓存计数增量后的实时热度分，口径与 SQL 里的衰减公式一致
     */
    private static double liveTrendScore(ArticleEntity article, ArticleCountSnapshot counts, long now) {
        long id = article.getId();
        return VideoRanking.trendScore(article, new VideoRanking.PendingCounts(
                counts.playCount(id),
                counts.likeCount(id),
                counts.favoriteCount(id),
                counts.commentCount(id),
                counts.danmakuCount(id),
                counts.dislikeCount(id)), now);
    }
}
