package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.WebConfigData;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 17:52
 */
@Component
@Data
public class WebConfigCache {
    /**
     * 原始配置数据
     * */
    private WebConfigData webConfigData;

    /**
     * 脱敏后的用户数据
     * */
    private WebConfigData userWebConfigData;


    public static int listMaxCount = 50;


    public void update(WebConfigData webConfigData) {
        this.webConfigData = webConfigData;
        this.userWebConfigData = webConfigData.copy().clean();
        listMaxCount = webConfigData.getHomeMaxVideoCount();
    }
}
