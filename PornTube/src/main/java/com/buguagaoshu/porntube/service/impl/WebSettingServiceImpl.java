package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.WebSettingDao;
import com.buguagaoshu.porntube.entity.WebSettingEntity;
import com.buguagaoshu.porntube.service.WebSettingService;


@Service("webSettingService")
public class WebSettingServiceImpl extends ServiceImpl<WebSettingDao, WebSettingEntity> implements WebSettingService {

    private WebSettingCache webSettingCache;

    @Autowired
    public void setWebSettingCache(WebSettingCache webSettingCache) {
        this.webSettingCache = webSettingCache;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WebSettingEntity> page = this.page(
                new Query<WebSettingEntity>().getPage(params),
                new QueryWrapper<WebSettingEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public WebSettingEntity getNewSetting() {
        return baseMapper.findNewSetting();
    }

    @Override
    public ReturnCodeEnum saveSetting(WebSettingEntity webSettingEntity) {
        webSettingEntity.setCreateTime(System.currentTimeMillis());
        webSettingEntity.setId(null);
        this.save(webSettingEntity);
        webSettingCache.setWebSettingEntity(webSettingEntity);
        return ReturnCodeEnum.SUCCESS;
    }

}