package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 播放记录
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("play_recording")
public class PlayRecordingEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 视频ID
	 */
	private Long articleId;

	/**
	 * 观看到第几个视频
	 */
	private Long fileId;

	/**
	 * 时间戳
	 */
	private Double videoTime;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Long createTime;

	/**
	 * 更新时间
	 */
	private Long updateTime;


	private long videoId;

	private String ua;

}
