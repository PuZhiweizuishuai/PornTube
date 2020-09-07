package com.buguagaoshu.porntube.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.LikeTableDao;
import com.buguagaoshu.porntube.entity.LikeTableEntity;
import com.buguagaoshu.porntube.service.LikeTableService;


@Service("likeTableService")
public class LikeTableServiceImpl extends ServiceImpl<LikeTableDao, LikeTableEntity> implements LikeTableService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<LikeTableEntity> page = this.page(
                new Query<LikeTableEntity>().getPage(params),
                new QueryWrapper<LikeTableEntity>()
        );

        return new PageUtils(page);
    }

}