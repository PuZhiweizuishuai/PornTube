package com.buguagaoshu.porntube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
@Data
@TableName("web_setting")
public class WebSettingEntity {
	/**
	 * 网站名
	 */
	private String name;

	/**
	 * 是否开启非vip每日观看次数限制 [0 关闭， 1 开启]
	 */
	private Integer openNoVipLimit;

	/**
	 * 非vip 每日观看次数
	 */
	private Integer noVipViewCount;

	/**
	 * 网页logo地址
	 */
	private String logoUrl;

	/**
	 * 是否开启邀请码注册 【0 关闭， 1开启】
	 */
	private Integer openInvitationRegister;

	/**
	 * 网页简短的描述
	 */
	private String webDescribe;

	/**
	 * 是否开启每日上传视频增加非会员观看次数 【0 关闭， 1开启】
	 */
	private Integer openUploadVideoAddViewCount;

	/**
	 * 是否开启视频，文章，图片盛和 【0 关闭， 1 开启】
	 */
	private Integer openExamine;

	/**
	 * 
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;

	/**
	 * 创建时间
	 */
	private Long createTime;



	private Integer homeMaxVideoCount;

}
