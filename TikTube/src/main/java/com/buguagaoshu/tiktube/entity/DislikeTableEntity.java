package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;

/**
 * 点踩
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("dislike_table")
public class DislikeTableEntity {
	/**
	 * 
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 
	 */
	private Long dislikeObjId;

	/**
	 * 
	 */
	private Long userId;

	/**
	 * 
	 */
	private Long createTime;

	/**
	 * 【0 帖子视频图片， 1 评论】
	 */
	private Integer type;

}
