package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.cache.SharedCache;
import com.buguagaoshu.tiktube.cache.SharedCacheVersionStore;
import com.buguagaoshu.tiktube.cache.WebConfigCache;
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

    private final WebConfigCache webConfigCache;

    private final MailService mailService;

    private final SharedCacheVersionStore versionStore;

    @Autowired
    public WebConfigService(WebConfigCache webConfigCache,
                            MailService mailService,
                            SharedCacheVersionStore versionStore) {
        this.webConfigCache = webConfigCache;
        this.mailService = mailService;
        this.versionStore = versionStore;
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
        applyConfig(webConfigData);
        return webConfigData;
    }

    /**
     * 从库里重新取一份设置并应用，供启动加载与其他实例的版本同步复用
     */
    public void refreshCache() {
        applyConfig(initConfig());
    }

    /**
     * 缓存更新与保存路径上的副作用统一走这里：远端实例同步时也要重新初始化邮件配置，
     * 否则跨实例修改 SMTP 后，那台机器仍会用旧参数发信
     */
    private void applyConfig(WebConfigData webConfigData) {
        webConfigCache.update(webConfigData);
        versionStore.markUpdated(SharedCache.WEB_CONFIG);
        if (webConfigData.getOpenEmail()) {
            mailService.initMainConfig();
        }
    }
    

}
