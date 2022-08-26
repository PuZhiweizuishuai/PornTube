package com.buguagaoshu.porntube.vo;

import com.buguagaoshu.porntube.valid.ListValue;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-08-25 15:59
 */
@Data
public class CommentVo {
    @NotNull(message = "目标帖子ID不能为空")
    private Long articleId;

    /**
     *
     */
    private Long userId;

    /**
     * 评论
     */
    @NotBlank(message = "评论正文不能为空！")
    private String comment;

    /**
     * 父级评论
     */
    private Long parentCommentId;

    /**
     * 评论对象
     */
    private Long parentUserId;


    private Long commentId;

    /**
     * 【1 一级评论  2 二级评论】
     */
    @ListValue(value = {1, 2})
    private Integer type;
}
