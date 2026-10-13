package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 一组指标的数值桶，用于「区间合计 / 上一周期合计」的对比
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorMetricBucket {

    private long view;

    private long like;

    private long comment;

    private long favorite;

    private long danmaku;

    private long fans;

    public void accumulate(CreatorMetricBucket other) {
        if (other == null) {
            return;
        }
        this.view += other.getView();
        this.like += other.getLike();
        this.comment += other.getComment();
        this.favorite += other.getFavorite();
        this.danmaku += other.getDanmaku();
        this.fans += other.getFans();
    }
}
