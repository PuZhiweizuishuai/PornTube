package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 收藏夹（含收藏数量）
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-14
 */
@Data
public class FavoritesLabelWithCount {
    private Long id;

    private String name;

    private Long userId;

    /**
     * 是否公开【0 私密， 1 公开】
     */
    private Integer isPublic;

    /**
     * 是否默认收藏夹【0 否， 1 是】
     */
    private Integer isDefault;

    private Long createTime;

    /**
     * 收藏夹内稿件数量
     */
    private Long count;
}
