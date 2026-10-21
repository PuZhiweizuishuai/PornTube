package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.WebConfigData;
import org.springframework.stereotype.Component;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 17:52
 */
@Component
public class WebConfigCache {

    /**
     * 原始配置与脱敏副本必须由同一次 update 一起换掉：分开赋值时请求线程可能
     * 拿着新原始配置配旧脱敏副本（或反过来）对外输出
     */
    private volatile Snapshot snapshot = new Snapshot(null, null);

    /**
     * 首页每页条数上限，只被 {@code Query} 当作分页钳制用，瞬时旧值没有危害，保持静态读取
     */
    public static volatile int listMaxCount = 50;


    public void update(WebConfigData webConfigData) {
        snapshot = new Snapshot(webConfigData, webConfigData.copy().clean());
        listMaxCount = webConfigData.getHomeMaxVideoCount();
    }

    /**
     * 原始配置数据
     * */
    public WebConfigData getWebConfigData() {
        return snapshot.webConfigData();
    }

    /**
     * 脱敏后的用户数据
     * */
    public WebConfigData getUserWebConfigData() {
        return snapshot.userWebConfigData();
    }

    private record Snapshot(WebConfigData webConfigData, WebConfigData userWebConfigData) {
    }
}
