package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.buguagaoshu.tiktube.dao.CreatorStatsDao;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.model.CreatorDailyPoint;
import com.buguagaoshu.tiktube.model.CreatorTotals;
import com.buguagaoshu.tiktube.model.DailyCountRow;
import com.buguagaoshu.tiktube.service.CreatorStatsService;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;
import com.buguagaoshu.tiktube.vo.CreatorArticleStat;
import com.buguagaoshu.tiktube.vo.CreatorCategoryStat;
import com.buguagaoshu.tiktube.vo.CreatorDataSummary;
import com.buguagaoshu.tiktube.vo.CreatorMetricBucket;
import com.buguagaoshu.tiktube.vo.CreatorOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 创作中心数据统计
 *
 * 时间窗口统一按「本地自然日」切分：先把当前时间对齐到当地 00:00，
 * 再往前推 days - 1 天作为起点，保证「近 7 天」就是今天 + 前 6 天。
 *
 * 所有方法都以 userId 为唯一数据边界，不存在跨用户查询。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Service
public class CreatorStatsServiceImpl implements CreatorStatsService {

    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000L;

    /**
     * 稿件榜的排序列白名单：前端传 key，这里映射成固定 SQL 片段。
     * 与项目 M8 安全整改的口径一致 —— 绝不把用户输入拼进 ORDER BY。
     */
    private static final Map<String, String> ORDER_SQL = Map.of(
            "periodView", "periodView DESC, viewCount DESC",
            "periodLike", "periodLike DESC, likeCount DESC",
            "periodComment", "periodComment DESC, commentCount DESC",
            "periodFavorite", "periodFavorite DESC, favoriteCount DESC",
            "periodDanmaku", "periodDanmaku DESC, danmakuCount DESC",
            "view", "a.view_count DESC",
            "like", "a.like_count DESC",
            "comment", "a.comment_count DESC",
            "createTime", "a.create_time DESC"
    );

    private static final String DEFAULT_ORDER_SQL = "a.create_time DESC";

    private final CreatorStatsDao creatorStatsDao;

    private final UserService userService;

    private final NotificationService notificationService;

    @Autowired
    public CreatorStatsServiceImpl(CreatorStatsDao creatorStatsDao,
                                   UserService userService,
                                   NotificationService notificationService) {
        this.creatorStatsDao = creatorStatsDao;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Override
    public CreatorOverview overview(long userId) {
        CreatorTotals totals = safeTotals(userId);

        CreatorOverview overview = new CreatorOverview();
        overview.setArticleCount(totals.getArticleCount());
        overview.setPendingCount(totals.getPendingCount());
        overview.setPassedCount(totals.getPassedCount());
        overview.setRejectedCount(totals.getRejectedCount());
        overview.setViewCount(totals.getViewCount());
        overview.setLikeCount(totals.getLikeCount());
        overview.setCommentCount(totals.getCommentCount());
        overview.setFavoriteCount(totals.getFavoriteCount());
        overview.setDanmakuCount(totals.getDanmakuCount());
        overview.setLastPublishTime(totals.getLastPublishTime());

        UserEntity user = userService.getById(userId);
        overview.setFansCount(user == null || user.getFansCount() == null ? 0 : user.getFansCount());
        overview.setUnreadNotification(notificationService.countUnread(userId));

        // 近 7 日趋势：既给图表用，也顺手汇总出「近 7 日新增」
        List<CreatorDailyPoint> week = dailySeries(userId, 7, 0);
        overview.setTrend(week);
        CreatorMetricBucket weekBucket = sum(week);
        overview.setWeekView(weekBucket.getView());
        overview.setWeekLike(weekBucket.getLike());
        overview.setWeekComment(weekBucket.getComment());
        overview.setWeekFavorite(weekBucket.getFavorite());
        overview.setWeekDanmaku(weekBucket.getDanmaku());
        overview.setWeekFans(weekBucket.getFans());

        // 今日新增就是趋势序列的最后一天
        if (!week.isEmpty()) {
            CreatorDailyPoint today = week.get(week.size() - 1);
            overview.setTodayView(today.getView());
            overview.setTodayLike(today.getLike());
            overview.setTodayComment(today.getComment());
            overview.setTodayFavorite(today.getFavorite());
            overview.setTodayDanmaku(today.getDanmaku());
            overview.setTodayFans(today.getFans());
        }
        return overview;
    }

    @Override
    public CreatorDataSummary summary(long userId, Integer days) {
        int normalized = normalizeDays(days);
        long now = System.currentTimeMillis();

        CreatorDataSummary summary = new CreatorDataSummary();
        summary.setDays(normalized);
        summary.setStartTime(windowStart(now, normalized));
        summary.setEndTime(windowEnd(now));

        List<CreatorDailyPoint> trend = dailySeries(userId, normalized, 0);
        summary.setTrend(trend);
        summary.setTotals(sum(trend));

        // 上一周期：紧挨着当前窗口的等长区间
        List<CreatorDailyPoint> previousTrend = dailySeries(userId, normalized, normalized);
        summary.setPrevious(sum(previousTrend));
        summary.setPeak(peak(trend));
        return summary;
    }

    @Override
    public PageUtils articleStats(Map<String, Object> params, long userId, Integer days, String sort) {
        int normalized = normalizeDays(days);
        long now = System.currentTimeMillis();
        long start = windowStart(now, normalized);
        long end = windowEnd(now);

        // 排序键不在白名单里就回落默认排序，非法值不报错也不拼进 SQL
        String orderSql = sort == null ? DEFAULT_ORDER_SQL : ORDER_SQL.getOrDefault(sort, DEFAULT_ORDER_SQL);

        IPage<CreatorArticleStat> page = creatorStatsDao.selectArticleStats(
                new Query<CreatorArticleStat>().getPage(params),
                userId,
                start,
                end,
                ArticleStatusEnum.NORMAL.getCode(),
                orderSql
        );
        if (page.getRecords() != null) {
            page.getRecords().forEach(CreatorArticleStat::fillEngagementRate);
        }
        return new PageUtils(page);
    }

    @Override
    public List<CreatorCategoryStat> categoryStats(long userId, Integer days) {
        int normalized = normalizeDays(days);
        long now = System.currentTimeMillis();
        return creatorStatsDao.selectCategoryStats(
                userId,
                windowStart(now, normalized),
                windowEnd(now),
                ArticleStatusEnum.NORMAL.getCode()
        );
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /**
     * 把前端传来的天数收敛到 [1, MAX_DAYS]，非法值回落默认天数
     * （接口里的 static 方法不会被继承，这里包一层）
     */
    private static int normalizeDays(Integer days) {
        return CreatorStatsService.normalizeDays(days);
    }

    /**
     * 生成一段连续的每日序列，缺失天补 0
     *
     * @param days      天数
     * @param shiftDays 往回偏移多少个「整周期」，0 表示当前周期，days 表示上一个周期
     */
    private List<CreatorDailyPoint> dailySeries(long userId, int days, int shiftDays) {
        long now = System.currentTimeMillis();
        long start = windowStart(now, days) - shiftDays * DAY_MILLIS;
        long end = start + days * DAY_MILLIS;

        List<DailyCountRow> rows = creatorStatsDao.selectDailyCounts(
                userId,
                start,
                end,
                TimeZone.getDefault().getRawOffset(),
                DAY_MILLIS,
                ArticleStatusEnum.NORMAL.getCode()
        );

        List<CreatorDailyPoint> series = new ArrayList<>(days);
        Map<Long, CreatorDailyPoint> byDay = new HashMap<>();
        for (int i = 0; i < days; i++) {
            long date = start + (long) i * DAY_MILLIS;
            CreatorDailyPoint point = new CreatorDailyPoint();
            point.setDate(date);
            series.add(point);
            byDay.put(dayIndexOf(date), point);
        }
        if (rows != null) {
            for (DailyCountRow row : rows) {
                CreatorDailyPoint point = byDay.get(row.getDayIndex());
                // 落在窗口外的脏数据（理论上不会有）直接丢弃
                if (point != null) {
                    point.add(row.getMetric(), row.getCnt());
                }
            }
        }
        return series;
    }

    private static CreatorMetricBucket sum(List<CreatorDailyPoint> series) {
        CreatorMetricBucket bucket = new CreatorMetricBucket();
        if (series == null) {
            return bucket;
        }
        for (CreatorDailyPoint point : series) {
            bucket.setView(bucket.getView() + point.getView());
            bucket.setLike(bucket.getLike() + point.getLike());
            bucket.setComment(bucket.getComment() + point.getComment());
            bucket.setFavorite(bucket.getFavorite() + point.getFavorite());
            bucket.setDanmaku(bucket.getDanmaku() + point.getDanmaku());
            bucket.setFans(bucket.getFans() + point.getFans());
        }
        return bucket;
    }

    private static CreatorDailyPoint peak(List<CreatorDailyPoint> series) {
        CreatorDailyPoint best = null;
        if (series == null) {
            return null;
        }
        for (CreatorDailyPoint point : series) {
            if (best == null || point.getView() > best.getView()) {
                best = point;
            }
        }
        return best;
    }

    private CreatorTotals safeTotals(long userId) {
        CreatorTotals totals = creatorStatsDao.selectTotals(userId, ArticleStatusEnum.NORMAL.getCode());
        return totals == null ? new CreatorTotals() : totals;
    }

    /** 时间窗口起点：todayStart - (days - 1) 天 */
    private static long windowStart(long now, int days) {
        return dayStart(now) - (long) (days - 1) * DAY_MILLIS;
    }

    /** 时间窗口终点（不含）：明天 00:00 */
    private static long windowEnd(long now) {
        return dayStart(now) + DAY_MILLIS;
    }

    /** 把时间戳对齐到当地 00:00 */
    private static long dayStart(long millis) {
        long offset = TimeZone.getDefault().getRawOffset();
        return ((millis + offset) / DAY_MILLIS) * DAY_MILLIS - offset;
    }

    /** 与 SQL 里 (时间 + tzOffset) DIV dayMillis 同一套算法 */
    private static long dayIndexOf(long millis) {
        return (millis + TimeZone.getDefault().getRawOffset()) / DAY_MILLIS;
    }
}
