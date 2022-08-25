package com.buguagaoshu.porntube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 弹幕表
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-07 16:00:19
 */
@Data
@TableName("danmaku")
public class DanmakuEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 视频ID
	 */
	private Long videoId;

	/**
	 * 弹幕颜色
	 */
	private String color;

	private Long author;

	/**
	 * 弹幕内容
	 */
	private String text;

	/**
	 * 时间
	 */
	private Double time;

	/**
	 * 类型
	 */
	private Integer type;


	/**
	 * 十进制颜色数据
	 * */
	private Long colorDec;


	private String ip;

	private String ua;

	private String city;

}
