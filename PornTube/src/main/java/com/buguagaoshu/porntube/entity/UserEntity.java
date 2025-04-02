package com.buguagaoshu.porntube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("user")
public class UserEntity {

	/**
	 * 用户ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户名
	 */
	@NotBlank(message = "用户名不能为空")
	private String username;

	/**
	 * 邮箱
	 */
	@Email(message = "邮箱格式不正确")
	@NotBlank(message = "邮箱不能为空")
	private String mail;

	/**
	 * 密码
	 */
	@NotBlank(message = "密码不能为空")
	private String password;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 创建时间
	 */
	private Long createTime;

	/**
	 * 提交视频，图片，文章数
	 */
	private Long submitCount;

	/**
	 * 关注数
	 */
	private Long followCount;

	/**
	 * 粉丝数
	 */
	private Long fansCount;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 简介
	 * */
	private String introduction;

	/**
	 * 首页大图url
	 */
	private String topImgUrl;


	@NotBlank(message = "验证码不为空")
	@TableField(exist = false)
	private String verifyCode;


	@TableField(exist = false)
	private String invitationCode;
}
