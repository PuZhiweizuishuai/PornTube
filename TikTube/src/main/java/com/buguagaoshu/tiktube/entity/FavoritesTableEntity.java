package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 收藏夹
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("favorites_table")
public class FavoritesTableEntity {
	/**
	 * 
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 
	 */
	private Long articeId;

	/**
	 * 
	 */
	private Long userId;


	private Long favoritesLabelId;

	/**
	 * 
	 */
	private Long createTime;

}
