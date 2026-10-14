package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.buguagaoshu.tiktube.entity.FavoritesLabelEntity;
import com.buguagaoshu.tiktube.vo.FavoritesLabelWithCount;

import java.util.List;

/**
 * 收藏夹分组
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-14
 */
public interface FavoritesLabelService extends IService<FavoritesLabelEntity> {

    /**
     * 获取（不存在则懒创建）用户的默认收藏夹
     * */
    FavoritesLabelEntity getOrCreateDefaultLabel(Long userId);

    /**
     * 用户的全部收藏夹（含收藏数量，默认收藏夹在最前）
     * */
    List<FavoritesLabelWithCount> listWithCount(Long userId);

    /**
     * 某用户公开的收藏夹（含收藏数量）
     * */
    List<FavoritesLabelWithCount> publicLabelList(Long userId);

    /**
     * 同一用户下收藏夹名称是否已存在（excludeId 用于改名时排除自身）
     * */
    boolean existsName(Long userId, String name, Long excludeId);

    /**
     * 删除收藏夹：默认收藏夹不可删除，其余收藏夹内的收藏移入默认收藏夹
     * */
    boolean deleteLabel(Long userId, Long labelId);
}
