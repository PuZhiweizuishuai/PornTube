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

    /**
     * 分类列表与两个索引必须一起替换：分开赋值时请求线程可能读到新列表配旧索引，
     * 而且跨实例同步是在调度线程里重建，字段本身还需要 volatile 保证可见性
     */
    private volatile Snapshot snapshot = new Snapshot(List.of(), Map.of(), Map.of());

    public void update(List<CategoryEntity> categoryEntities,
                       Map<Integer, CategoryEntity> categoryEntityMap,
                       Map<Integer, CategoryEntity> categoryMapWithChildren) {
        snapshot = new Snapshot(categoryEntities, categoryEntityMap, categoryMapWithChildren);
    }

    public List<CategoryEntity> getCategoryEntities() {
        return snapshot.categoryEntities();
    }

    public Map<Integer, CategoryEntity> getCategoryEntityMap() {
        return snapshot.categoryEntityMap();
    }

    public Map<Integer, CategoryEntity> getCategoryMapWithChildren() {
        return snapshot.categoryMapWithChildren();
    }

    private record Snapshot(List<CategoryEntity> categoryEntities,
                            Map<Integer, CategoryEntity> categoryEntityMap,
                            Map<Integer, CategoryEntity> categoryMapWithChildren) {
    }
}
