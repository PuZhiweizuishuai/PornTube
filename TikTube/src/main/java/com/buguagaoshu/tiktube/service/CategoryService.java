package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 18:32:00
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    Map<Integer, CategoryEntity> categoryEntityMapWithChildren(List<CategoryEntity> entities);

    List<CategoryEntity> listWithTree();

    Map<Integer, CategoryEntity> categoryEntityMap();
}

