package com.buguagaoshu.tiktube.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.DislikeTableDao;
import com.buguagaoshu.tiktube.entity.DislikeTableEntity;
import com.buguagaoshu.tiktube.service.DislikeTableService;


@Service("dislikeTableService")
public class DislikeTableServiceImpl extends ServiceImpl<DislikeTableDao, DislikeTableEntity> implements DislikeTableService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<DislikeTableEntity> page = this.page(
                new Query<DislikeTableEntity>().getPage(params),
                new QueryWrapper<DislikeTableEntity>()
        );

        return new PageUtils(page);
    }

}