package com.buguagaoshu.porntube.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.FavoritesTableDao;
import com.buguagaoshu.porntube.entity.FavoritesTableEntity;
import com.buguagaoshu.porntube.service.FavoritesTableService;


@Service("favoritesTableService")
public class FavoritesTableServiceImpl extends ServiceImpl<FavoritesTableDao, FavoritesTableEntity> implements FavoritesTableService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<FavoritesTableEntity> page = this.page(
                new Query<FavoritesTableEntity>().getPage(params),
                new QueryWrapper<FavoritesTableEntity>()
        );

        return new PageUtils(page);
    }

}