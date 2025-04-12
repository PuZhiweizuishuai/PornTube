package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.FavoritesTableEntity;

import java.util.Map;

/**
 * 收藏夹
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface FavoritesTableService extends IService<FavoritesTableEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 新增收藏
     * TODO 收藏夹功能
     * */
    FavoritesTableEntity toggleFavorites(FavoritesTableEntity favorite, Long userId);


    /**
     * 检查当前稿件是否被收藏
     * */
    boolean checkFavorites(Long articleId, Long userId);


    /**
     * 用户收藏列表
     * */
    PageUtils userFavorites(Map<String, Object> params, Long userId);
}

