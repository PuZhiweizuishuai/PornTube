package com.buguagaoshu.tiktube.model;

import lombok.Data;

/**
 * 创作者稿件的累计统计（不含时间维度）
 *
 * 全部来自 article 表的计数列，只统计未删除（status = 0）的稿件。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorTotals {

    private long articleCount;

    /** 审核中 */
    private long pendingCount;

    /** 已通过 */
    private long passedCount;

    /** 未通过（examine_status &gt; 1） */
    private long rejectedCount;

    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    /** 最近一次发布时间，没有稿件时为 0 */
    private long lastPublishTime;
}
