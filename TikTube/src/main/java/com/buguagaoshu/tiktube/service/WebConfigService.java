package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.config.WebConfigData;
import com.buguagaoshu.tiktube.dao.WebConfigDao;
import com.buguagaoshu.tiktube.entity.WebConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * &#064;create  2025-05-05
 */
@Service
@Slf4j
public class WebConfigService extends ServiceImpl<WebConfigDao, WebConfigEntity> {

    private final WebSettingCache webSettingCache;

    private final MailService mailService;

    @Autowired
    public WebConfigService(WebSettingCache webSettingCache,
                            MailService mailService) {
        this.webSettingCache = webSettingCache;
        this.mailService = mailService;
    }

    /**
     * 获取最新的网页配置
     * */
    public WebConfigEntity findNewWebConfig() {
        QueryWrapper<WebConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 1");
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 获取初始化配置
     * */
    public WebConfigData initConfig() {
        WebConfigEntity webConfig = findNewWebConfig();
        if (webConfig == null) {
            // 保存默认配置
            WebConfigData webConfigData = WebConfigData.defaultConfig();
            saveWebConfig(webConfigData, 0, "SYSTEM_INIT");
            return webConfigData;
        }
        return WebConfigData.analysisData(webConfig.getJsonText());
    }


    /**
     * 保存最新网页数据
     * */
    public WebConfigData saveWebConfig(WebConfigData webConfigData, long userId, String ip) {
        WebConfigEntity webConfigEntity = new WebConfigEntity();
        webConfigEntity.setIp(ip);
        webConfigEntity.setCreateTime(System.currentTimeMillis());
        webConfigEntity.setJsonText(WebConfigData.createSaveData(webConfigData));
        webConfigEntity.setCreateUser(userId);
        this.save(webConfigEntity);
        // 更新缓存
        webSettingCache.update(webConfigData);
        if (webConfigData.getOpenEmail()) {
            mailService.initMainConfig();
        }
        return webConfigData;
    }
    

}
