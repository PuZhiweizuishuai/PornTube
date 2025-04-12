package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.entity.WebSettingEntity;
import com.buguagaoshu.tiktube.service.WebSettingService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
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
