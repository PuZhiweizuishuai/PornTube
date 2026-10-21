package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.spring.service.IService;
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

    /**
     * 从库里重新取一遍分类并替换内存快照，同时把新版本广播给其他实例
     * */
    void refreshCache();


    /**
     * 删除分类
     * */
    Map<String, String> deleteCategory(Integer id);

    /**
     * 创建分类
     * */
    CategoryEntity saveCategory(CategoryEntity entity);


    /**
     * 修改分类
     * */
    Map<String, String> updateCategory(CategoryEntity entity);

}

