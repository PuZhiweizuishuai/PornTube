package com.buguagaoshu.tiktube.vo;

import com.buguagaoshu.tiktube.model.CreatorDailyPoint;
import lombok.Data;

import java.util.List;

/**
 * 创作中心「信息中心」总览
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorOverview {

    /** 粉丝总数 */
    private long fansCount;

    /** 稿件总数（未删除） */
    private long articleCount;

    private long pendingCount;

    private long passedCount;

    private long rejectedCount;

    // ---- 累计 ----
    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    // ---- 今日新增 ----
    private long todayView;

    private long todayLike;

    private long todayComment;

    private long todayFavorite;

    private long todayDanmaku;

    private long todayFans;

    // ---- 近 7 日新增 ----
    private long weekView;

    private long weekLike;

    private long weekComment;

    private long weekFavorite;

    private long weekDanmaku;

    private long weekFans;

    /** 未读通知数 */
    private long unreadNotification;

    /** 最近发布时间，没有稿件时为 0 */
    private long lastPublishTime;

    /** 近 7 日趋势 */
    private List<CreatorDailyPoint> trend;
}
