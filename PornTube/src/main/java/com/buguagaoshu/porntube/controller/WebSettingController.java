package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.entity.WebSettingEntity;
import com.buguagaoshu.porntube.service.WebSettingService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 20:14
 */
@RestController
public class WebSettingController {
    private final WebSettingCache webSettingCache;

    private final WebSettingService webSettingService;

    @Autowired
    public WebSettingController(WebSettingCache webSettingCache, WebSettingService webSettingService) {
        this.webSettingCache = webSettingCache;
        this.webSettingService = webSettingService;
    }

    @GetMapping("/api/web/info")
    public ResponseDetails webInfo() {
        return ResponseDetails.ok().put("data", webSettingCache.getWebSettingEntity());
    }


    @PostMapping("/api/admin/setting/save")
    public ResponseDetails updateWebSetting(@RequestBody WebSettingEntity WebSettingEntity) {
        return ResponseDetails.ok(webSettingService.saveSetting(WebSettingEntity));
    }
}
