package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;

/**
 * 视频，图片，文章 发帖表

TODO 回复消息可见，加密帖子，视频等
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("article")
public class ArticleEntity {
	/**
	 * 
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

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
	 * 弹幕数
	 * */
	private Long danmakuCount;

	/**
	 * 不喜欢次数
	 */
	private Long dislikeCount;

	/**
	 * 评论数
	 * */
	private Long commentCount;

	/**
	 * 审核情况 【0 暂未审核， 1 通过， 2 不通过】向西见枚举类
	 */
	private Integer examineStatus;

	/**
	 * 审核意见
	 */
	private String examineMessage;

	/**
	 * 审核人
	 * */
	private Long examineUser;

	/**
	 * 分区
	 */
	private Integer category;

	/**
	 * 标签
	 */
	private String tag;

	/**
	 * 发帖人ID
	 */
	private Long userId;

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

	/**
	 * [0 正常， 1 删除]
	 */
	private Integer status;


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


	private String ua;

	private String ip;

	private String city;

	@TableField(exist = false)
	private Double sort;
}
