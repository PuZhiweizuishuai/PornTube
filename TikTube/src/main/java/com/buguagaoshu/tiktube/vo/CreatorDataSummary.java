package com.buguagaoshu.tiktube.vo;

import com.buguagaoshu.tiktube.model.CreatorDailyPoint;
import lombok.Data;

import java.util.List;

/**
 * 创作中心「数据分析」区间汇总
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorDataSummary {

    /** 统计天数 */
    private int days;

    private long startTime;

    private long endTime;

    /** 区间内的每日趋势 */
    private List<CreatorDailyPoint> trend;

    /** 区间合计 */
    private CreatorMetricBucket totals;

    /** 上一个等长周期的合计，用于算环比 */
    private CreatorMetricBucket previous;

    /** 播放（观看人数）最高的一天 */
    private CreatorDailyPoint peak;
}
