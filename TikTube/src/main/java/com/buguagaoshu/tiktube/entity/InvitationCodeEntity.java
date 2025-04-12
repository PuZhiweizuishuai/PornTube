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
@TableName("invitation_code")
public class InvitationCodeEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 邀请码
	 */
	private String code;

	/**
	 * 生成邀请码的人
	 */
	private Long createUser;

	/**
	 * 使用邀请吗的人
	 */
	private Long useUser;

	/**
	 * 【1 未被使用， 0 已经被使用】
	 */
	private Integer useStatus;

	/**
	 * 生成时间
	 */
	private Long createTime;

	/**
	 * 使用时间
	 */
	private Long useTime;

}
