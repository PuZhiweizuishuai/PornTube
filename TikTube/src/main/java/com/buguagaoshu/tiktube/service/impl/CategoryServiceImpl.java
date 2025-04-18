package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.CategoryCache;
import com.buguagaoshu.tiktube.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.CategoryDao;
import com.buguagaoshu.tiktube.entity.CategoryEntity;
import com.buguagaoshu.tiktube.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    final CategoryCache categoryCache;

    final ArticleService articleService;

    @Autowired
    public CategoryServiceImpl(CategoryCache categoryCache, ArticleService articleService) {
        this.categoryCache = categoryCache;
        this.articleService = articleService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Map<Integer, CategoryEntity> categoryEntityMapWithChildren(List<CategoryEntity> entities) {
        return entities.stream().collect(Collectors.toMap(CategoryEntity::getId, c -> c));
    }

    @Override
    public Map<Integer, CategoryEntity> categoryEntityMap() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream().collect(Collectors.toMap(CategoryEntity::getId, c -> c));
    }

    @Override
    public Map<String, String> deleteCategory(Integer id) {
        Map<String, String> result = new HashMap<>();
        // 检查分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(id);
        if (categoryEntity == null) {
            result.put("info", "当前分类不存在！");
            return result;
        }
        // 如果是一级分类，则需要检查子分类
        if (categoryEntity.getType().equals(1)) {
            if (!categoryCache.getCategoryMapWithChildren().get(categoryEntity.getId()).getChildren().isEmpty()) {
                result.put("info", "存在子分类，无法删除，请先删除子分类！");
                return result;
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 2);
        PageUtils pageUtils = articleService.nowCategory(params, id);

        if (!pageUtils.getList().isEmpty()) {
            result.put("info", "当前分类下已经存在视频，无法删除，只能修改！");
            return result;
        }
        result.put("info", "删除成功！");
        this.removeById(categoryEntity);
        updateCache();
        return result;
    }

    @Override
    public CategoryEntity saveCategory(CategoryEntity entity) {
        long time = System.currentTimeMillis();
        entity.setId(null);
        entity.setCreateTime(time);
        entity.setUpdateTime(time);
        if (entity.getFatherId() != null || entity.getType().equals(2)) {
            CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().put(entity.getFatherId(), entity);
            if (categoryEntity != null) {
                entity.setType(2);
            } else {
                entity.setType(1);
                entity.setFatherId(0);
            }
        }
        this.save(entity);
        // 更新缓存
        updateCache();
        return entity;
    }

    private void updateCache() {
        categoryCache.setCategoryEntities(listWithTree());
        categoryCache.setCategoryEntityMap(categoryEntityMap());
        categoryCache.setCategoryMapWithChildren(categoryEntityMapWithChildren(categoryCache.getCategoryEntities()));
    }

    @Override
    public Map<String, String> updateCategory(CategoryEntity entity) {
        Map<String, String> result = new HashMap<>();
        // 检查分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(entity.getId());
        if (categoryEntity == null) {
            result.put("info", "当前分类不存在！");
            return result;
        }
        entity.setUpdateTime(System.currentTimeMillis());

        result.put("info", "修改成功！");
        this.updateById(entity);
        updateCache();
        return result;
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        List<CategoryEntity> level1Menus =
                entities.stream().filter((categoryEntity -> categoryEntity.getFatherId() == 0))
                        .peek((menu) -> menu.setChildren(getChildren(menu, entities)))
                        .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                        .collect(Collectors.toList());
        return level1Menus;
    }


    private List<CategoryEntity> getChildren(CategoryEntity root,
                                             List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter((categoryEntity) -> categoryEntity.getFatherId().equals(root.getId()))
                .peek((categoryEntity) -> {
                    // 查找子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
        return children;
    }
}