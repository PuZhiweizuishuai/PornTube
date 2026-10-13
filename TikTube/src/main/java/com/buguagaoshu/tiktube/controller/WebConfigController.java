package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.TikTubeApplication;
import com.buguagaoshu.tiktube.cache.WebConfigCache;
import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.config.WebConfigData;
import com.buguagaoshu.tiktube.entity.OSSConfigEntity;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import com.buguagaoshu.tiktube.repository.impl.FileRepositoryInOSS;
import com.buguagaoshu.tiktube.service.WebConfigService;
import com.buguagaoshu.tiktube.service.impl.OssConfigService;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 20:14
 */
@RestController
public class WebConfigController {
    private final WebConfigCache webConfigCache;

    private final WebConfigService webConfigService;

    private final OssConfigService ossConfigService;

    private final FileRepositoryInOSS repositoryInOSS;


    private final IpUtil ipUtil;

    @Autowired
    public WebConfigController(WebConfigCache webConfigCache,
                                WebConfigService webConfigService,
                                OssConfigService ossConfigService,
                                FileRepositoryInOSS repositoryInOSS,
                                IpUtil ipUtil) {
        this.webConfigCache = webConfigCache;
        this.webConfigService = webConfigService;
        this.ossConfigService = ossConfigService;
        this.repositoryInOSS = repositoryInOSS;
        this.ipUtil = ipUtil;
    }


    @GetMapping("/api/version")
    public ResponseDetails webVersion() {
        return ResponseDetails.ok().put("version", TikTubeApplication.VERSION);
    }

    @GetMapping("/api/web/info")
    public ResponseDetails webInfo() {
        return ResponseDetails.ok().put("data", webConfigCache.getUserWebConfigData());
    }




    @PostMapping("/api/admin/setting/save")
    public ResponseDetails updateWebSetting(@Valid @RequestBody WebConfigData webConfigData,
                                            HttpServletRequest request) {
        WebConfigData data = webConfigService.saveWebConfig(webConfigData, JwtUtil.getUserId(request), ipUtil.getIpAddr(request));
        return ResponseDetails.ok().put("data", data);
    }

    @GetMapping("/api/admin/setting/info")
    public ResponseDetails webInfoForAdmin() {
        return ResponseDetails.ok().put("data", webConfigCache.getWebConfigData());
    }

    
    /**
     * 获取所有OSS配置列表
     */
    @GetMapping("/api/admin/oss/list")
    public ResponseDetails listOssConfigs() {
        return ResponseDetails.ok().put("data", ossConfigService.listAllOssConfigs());
    }

    /**
     * 获取单个OSS配置信息
     */
    @GetMapping("/api/admin/oss/{id}")
    public ResponseDetails getOssConfig(@PathVariable("id") Integer id) {
        return ResponseDetails.ok().put("data", ossConfigService.getOssConfigById(id));
    }

    /**
     * 保存OSS配置
     */
    @PostMapping("/api/admin/oss/save")
    public ResponseDetails saveOssConfig(@RequestBody OSSConfigEntity ossConfig,
                                         HttpServletRequest request) {
        // 设置创建者ID
        ossConfig.setCreatorId(JwtUtil.getUserId(request));
        // 设置更新者ID
        ossConfig.setUpdaterId(JwtUtil.getUserId(request));
        boolean b = ossConfigService.saveOssConfig(ossConfig);
        if (b) {
            repositoryInOSS.updateS3Client();
        }
        return ResponseDetails.ok().put("data", b);
    }

    /**
     * 更新OSS配置
     */
    @PostMapping("/api/admin/oss/update")
    public ResponseDetails updateOssConfig(@RequestBody OSSConfigEntity ossConfig,
                                           HttpServletRequest request) {
        // 设置更新者ID
        ossConfig.setUpdaterId(JwtUtil.getUserId(request));
        boolean b = ossConfigService.updateOssConfig(ossConfig);
        if (b) {
            repositoryInOSS.updateS3Client();
        }
        return ResponseDetails.ok().put("data", b);
    }

    @GetMapping("/api/admin/oss/location")
    public ResponseDetails getSaveLocation() {
        File diskPartition = new File("/");
        Map<String, Object> map = new HashMap<>();
        map.put("free", diskPartition.getFreeSpace());
        map.put("location", WebConfig.FILE_SAVE_LOCATION);
        return ResponseDetails.ok().put("data",map);
    }

    /**
     * 删除OSS配置
     */
    @PostMapping("/api/admin/oss/delete/{id}")
    public ResponseDetails deleteOssConfig(@PathVariable("id") Integer id) {
        boolean b = ossConfigService.deleteOssConfig(id);
        if (b) {
            repositoryInOSS.updateS3Client();
        }
        return ResponseDetails.ok().put("data", b);
    }

    /**
     * 验证OSS配置是否可用
     */
    @PostMapping("/api/admin/oss/validate")
    public ResponseDetails validateOssConfig(@RequestBody OSSConfigEntity ossConfig) {
        return ResponseDetails.ok().put("data", ossConfigService.validateOssConfig(ossConfig));
    }
}
