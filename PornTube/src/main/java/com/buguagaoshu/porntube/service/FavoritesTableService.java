package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.FavoritesTableEntity;

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
}

