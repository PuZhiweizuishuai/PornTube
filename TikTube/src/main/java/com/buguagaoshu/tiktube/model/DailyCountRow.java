package com.buguagaoshu.tiktube.model;

import lombok.Data;

/**
 * 按天聚合查询的一行：某个指标在某一天的新增量
 *
 * dayIndex 是「天序号」而不是时间戳，由 SQL 的
 * {@code (时间 + tzOffset) DIV dayMillis} 算出，服务层再还原成当天 00:00 的时间戳，
 * 这样就能彻底避开各数据库日期函数的差异。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class DailyCountRow {

    /** view / like / comment / favorite / danmaku / fans */
    private String metric;

    private long dayIndex;

    private long cnt;
}
