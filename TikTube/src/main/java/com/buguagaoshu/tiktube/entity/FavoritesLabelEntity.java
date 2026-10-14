package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 收藏夹分组
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-14
 */
@Data
@TableName("favorites_label")
public class FavoritesLabelEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 收藏夹名称
	 */
	private String name;

	private Long userId;

	/**
	 * 是否公开【0 私密， 1 公开，公开收藏夹在用户首页可见】
	 */
	private Integer isPublic;

	/**
	 * 是否默认收藏夹【0 否， 1 是，每个用户至多一个，不可删除】
	 */
	private Integer isDefault;

	private Long createTime;

}
