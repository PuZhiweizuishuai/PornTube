package com.buguagaoshu.tiktube.service;

import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.vo.CreatorArticleStat;
import com.buguagaoshu.tiktube.vo.CreatorCategoryStat;
import com.buguagaoshu.tiktube.vo.CreatorDataSummary;
import com.buguagaoshu.tiktube.vo.CreatorOverview;

import java.util.List;
import java.util.Map;

/**
 * 创作中心数据统计
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
public interface CreatorStatsService {

    /** 默认统计天数 */
    int DEFAULT_DAYS = 30;

    /** 最大统计天数，超过按它算，避免大区间把数据库拖垮 */
    int MAX_DAYS = 90;

    /**
     * 信息中心总览
     */
    CreatorOverview overview(long userId);

    /**
     * 数据分析区间汇总（合计 + 上一周期合计 + 每日趋势）
     *
     * @param days 统计天数，非法值回落到 {@link #DEFAULT_DAYS}
     */
    CreatorDataSummary summary(long userId, Integer days);

    /**
     * 稿件表现榜（分页）
     *
     * @param params 分页参数（page / limit）
     * @param sort   排序列键，只接受白名单内的值，其余回落到默认排序
     */
    PageUtils articleStats(Map<String, Object> params, long userId, Integer days, String sort);

    /**
     * 分区分布
     */
    List<CreatorCategoryStat> categoryStats(long userId, Integer days);

    /**
     * 把前端传来的天数收敛到 [1, MAX_DAYS]
     */
    static int normalizeDays(Integer days) {
        if (days == null || days <= 0) {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }

    /** 稿件榜允许的排序列，前端只能传这里的 key */
    List<String> ALLOWED_SORTS = List.of(
            "periodView", "periodLike", "periodComment", "periodFavorite", "periodDanmaku",
            "view", "like", "comment", "createTime"
    );
}
