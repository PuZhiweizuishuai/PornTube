package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 数据分析「稿件表现榜」的一行
 *
 * 累计值来自 article 表，区间值来自各事件表按 create_time 聚合。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorArticleStat {

    private Long id;

    private String title;

    private String imgUrl;

    /** 0 视频，1 图片，2 文章 */
    private Integer type;

    private Integer category;

    private Long createTime;

    private Long updateTime;

    private Double duration;

    private Integer examineStatus;

    // ---- 累计 ----
    private long viewCount;

    private long likeCount;

    private long commentCount;

    private long favoriteCount;

    private long danmakuCount;

    private long dislikeCount;

    // ---- 区间新增 ----
    private long periodView;

    private long periodLike;

    private long periodComment;

    private long periodFavorite;

    private long periodDanmaku;

    /**
     * 互动率 = (区间点赞 + 评论 + 收藏 + 弹幕) / 区间观看人数
     * 观看人数为 0 时返回 0，避免除零
     */
    private double engagementRate;

    public void fillEngagementRate() {
        if (periodView <= 0) {
            this.engagementRate = 0;
            return;
        }
        long interactions = periodLike + periodComment + periodFavorite + periodDanmaku;
        this.engagementRate = Math.round((interactions * 10000.0 / periodView)) / 10000.0;
    }
}
