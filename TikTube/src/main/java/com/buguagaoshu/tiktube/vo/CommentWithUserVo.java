package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-08-25 20:13
 */
@Data
public class CommentWithUserVo {
    private Long id;

    /**
     *
     */
    private Long articleId;

    /**
     *
     */
    private Long userId;

    /**
     * 评论
     */
    private String comment;

    private Long commentId;

    /**
     * 父级评论
     */
    private Long parentCommentId;

    /**
     * 评论对象
     */
    private Long parentUserId;

    private String parentUserName;

    /**
     * 喜欢数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 不喜欢
     */
    private Long dislikeCount;

    /**
     * 【1 一级评论  2 二级评论】
     */
    private Integer type;


    /**
     *
     */
    private Long createTime;

    /**
     *
     */
    private Long updateTime;


    private String ua;

    private String city;

    /// 用户信息部分


    private String username;

    /**
     * 关注数
     */
    private Long followCount;

    /**
     * 粉丝数
     */
    private Long fansCount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 简介
     * */
    private String introduction;
}
