package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.WebSettingEntity;
import org.springframework.stereotype.Component;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 17:52
 */
@Component
public class WebSettingCache {
    private WebSettingEntity webSettingEntity;

    public WebSettingEntity getWebSettingEntity() {
        return webSettingEntity;
    }

    public void setWebSettingEntity(WebSettingEntity webSettingEntity) {
        this.webSettingEntity = webSettingEntity;
    }
}
