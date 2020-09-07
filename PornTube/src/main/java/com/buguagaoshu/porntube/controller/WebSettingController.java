package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 20:14
 */
@RestController
public class WebSettingController {
    private final WebSettingCache webSettingCache;

    @Autowired
    public WebSettingController(WebSettingCache webSettingCache) {
        this.webSettingCache = webSettingCache;
    }

    @GetMapping("/api/web/info")
    public ResponseDetails webInfo() {
        return ResponseDetails.ok().put("data", webSettingCache.getWebSettingEntity());
    }
}
