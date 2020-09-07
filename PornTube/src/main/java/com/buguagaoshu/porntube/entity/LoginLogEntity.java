package com.buguagaoshu.porntube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("login_log")
public class LoginLogEntity {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 登录时间
	 */
	private Long loginTime;

	/**
	 * 登录IP
	 */
	private String ip;

	/**
	 * 浏览器UA
	 */
	private String ua;

	/**
	 * 登录用户ID
	 */
	private Long userid;

}
