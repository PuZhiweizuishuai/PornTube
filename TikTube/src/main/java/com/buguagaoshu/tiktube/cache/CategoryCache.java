package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 18:45
 */
@Component
public class CategoryCache {
    private List<CategoryEntity> categoryEntities;

    private Map<Integer, CategoryEntity> categoryEntityMap;

    private Map<Integer, CategoryEntity> categoryMapWithChildren;

    public Map<Integer, CategoryEntity> getCategoryMapWithChildren() {
        return categoryMapWithChildren;
    }

    public void setCategoryMapWithChildren(Map<Integer, CategoryEntity> categoryMapWithChildren) {
        this.categoryMapWithChildren = categoryMapWithChildren;
    }

    public List<CategoryEntity> getCategoryEntities() {
        return categoryEntities;
    }

    public void setCategoryEntities(List<CategoryEntity> categoryEntities) {
        this.categoryEntities = categoryEntities;
    }

    public Map<Integer, CategoryEntity> getCategoryEntityMap() {
        return categoryEntityMap;
    }

    public void setCategoryEntityMap(Map<Integer, CategoryEntity> categoryEntityMap) {
        this.categoryEntityMap = categoryEntityMap;
    }


}
