package com.buguagaoshu.porntube.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.CategoryDao;
import com.buguagaoshu.porntube.entity.CategoryEntity;
import com.buguagaoshu.porntube.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Map<Integer, CategoryEntity> categoryEntityMap() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream().collect(Collectors.toMap(CategoryEntity::getId, c -> c));
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