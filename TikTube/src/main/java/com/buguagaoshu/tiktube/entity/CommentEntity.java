package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("comment")
public class CommentEntity {
	@TableId(type = IdType.AUTO)
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

	/**
	 * 二级评论时根节点评论ID
	 * */
	private Long commentId;

	/**
	 * 父级评论
	 */
	private Long parentCommentId;

	/**
	 * 评论对象
	 */
	private Long parentUserId;

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
	 * [0 正常  1 删除]
	 */
	private Integer status;

	/**
	 * 
	 */
	private Long createTime;

	/**
	 * 
	 */
	private Long updateTime;

	private String ip;

	private String ua;

	private String city;


	public void initComment() {
		long time = System.currentTimeMillis();
		this.status = 0;
		this.likeCount = 0L;
		this.createTime = time;
		this.updateTime = time;
		this.commentCount = 0L;
	}
}
