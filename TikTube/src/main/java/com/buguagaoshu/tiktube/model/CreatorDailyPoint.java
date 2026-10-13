package com.buguagaoshu.tiktube.model;

import lombok.Data;

/**
 * 创作者数据统计的「单日数据点」
 *
 * 趋势图的一个 X 轴刻度。缺失的天由服务层补 0，保证前端拿到的序列连续。
 *
 * 口径说明：view 取 play_recording（用户 × 分P 唯一一条，重复观看只更新 update_time），
 * 因此它是「当日观看人数（去重）」而不是播放次数；其余五项都是事件表的真实新增量。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorDailyPoint {

    /** 当天 00:00（本地时区）的时间戳 */
    private long date;

    /** 观看人数（去重） */
    private long view;

    private long like;

    private long comment;

    private long favorite;

    private long danmaku;

    /** 新增粉丝 */
    private long fans;

    public void add(String metric, long count) {
        switch (metric) {
            case "view" -> this.view += count;
            case "like" -> this.like += count;
            case "comment" -> this.comment += count;
            case "favorite" -> this.favorite += count;
            case "danmaku" -> this.danmaku += count;
            case "fans" -> this.fans += count;
            default -> { /* 未知指标直接忽略，避免脏数据打断整个趋势 */ }
        }
    }

    public long total() {
        return view + like + comment + favorite + danmaku + fans;
    }
}
