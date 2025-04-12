package com.buguagaoshu.tiktube.vo;


import com.buguagaoshu.tiktube.entity.CategoryEntity;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 22:06
 * 播放视频需要的数据
 */
@Data
public class ArticleViewData {
    private Long id;

    private Boolean isShow = false;
    /**
     * 图片url
     */
    private String imgUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 简介
     */
    private String describes;

    /**
     * 观看次数
     */
    private Long viewCount;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 收藏次数
     */
    private Long favoriteCount;

    /**
     * 不喜欢次数
     */
    private Long dislikeCount;

    private Long commentCount;

    /**
     * 弹幕数
     * */
    private Long danmakuCount;

    /**
     * 分区
     */
    private CategoryEntity fatherCategory;


    private CategoryEntity childrenCategory;

    /**
     * 标签
     */
    private List<String> tag;

    /**
     * 发帖人ID
     */
    private Long userId;

    private String username;

    private String avatarUrl;

    private String introduction;

    private Long followCount;

    /**
     * 类型 【0 视频， 1 图片  2 文章】
     */
    private Integer type;

    /**
     * 发布时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;


    private Long score;

    private Long scoreCount;

    /**
     * 视频长度
     * */
    private Double duration;

    /**
     * 像素数
     * */
    private Long pixelsNumber;

    /**
     * 帧率
     * */
    private Double frameRate;

    private Double sort;

    private List<FileTableEntity> video;
}
