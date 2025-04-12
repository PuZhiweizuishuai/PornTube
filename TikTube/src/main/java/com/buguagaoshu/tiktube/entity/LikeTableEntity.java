package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;

/**
 * 点赞
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("like_table")
public class LikeTableEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 喜欢的对象ID
	 */
	private Long likeObjId;

	/**
	 * 
	 */
	private Long userId;

	/**
	 * 
	 */
	private Long createTime;

	/**
	 * 【0 视频图片文章主帖子  1 评论】
	 */
	private Integer type;

}
