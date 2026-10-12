package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.cache.VideoCatalogCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.schedule.CountTasks;
import com.buguagaoshu.tiktube.schedule.DeleteTempFileTasks;
import com.buguagaoshu.tiktube.service.AdvertisementService;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.SystemInfoService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SystemController {
    private final ArticleService articleService;

    private final DeleteTempFileTasks deleteTempFileTasks;

    private final CountTasks countTasks;

    private final AdvertisementService advertisementService;

    private final SystemInfoService systemInfoService;

    private final HotCache hotCache;

    private final VideoCatalogCache videoCatalogCache;

    @Autowired
    public SystemController(ArticleService articleService,
                            DeleteTempFileTasks deleteTempFileTasks,
                            CountTasks countTasks,
                            AdvertisementService advertisementService,
                            SystemInfoService systemInfoService,
                            HotCache hotCache,
                            VideoCatalogCache videoCatalogCache) {
        this.articleService = articleService;
        this.deleteTempFileTasks = deleteTempFileTasks;
        this.countTasks = countTasks;
        this.advertisementService = advertisementService;
        this.systemInfoService = systemInfoService;
        this.hotCache = hotCache;
        this.videoCatalogCache = videoCatalogCache;
    }


    @GetMapping("/admin/system/info")
    public ResponseDetails getSystemInfo() {
        return ResponseDetails.ok().put("data", systemInfoService.getSystemRunInfo());
    }


    @PostMapping("/admin/system/refresh/hot")
    public ResponseDetails refreshHot(HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        log.info("管理员：id:{} name: {} 手动刷新热门数据", user.getId(), user.getSubject());
        hotCache.update(articleService.hotView(WebConstant.HOT_NUM));
        videoCatalogCache.refresh();
        return ResponseDetails.ok();
    }

    /**
     * 手动同步数据
     * */
    @PostMapping("/admin/system/data/sync")
    public ResponseDetails syncCount(HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        log.info("管理员：id:{} name: {} 手动同步缓存数据", user.getId(), user.getSubject());
        countTasks.saveCount();
        return ResponseDetails.ok();
    }


    @PostMapping("/admin/system/ads/sync")
    public ResponseDetails syncAds(HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        log.info("管理员：id:{} name: {} 手动同步广告缓存数据", user.getId(), user.getSubject());
        advertisementService.getNowAdvertisement();
        return ResponseDetails.ok();
    }


    /**
     * 手动删除临时文件
     * */
    @PostMapping("/admin/system/file/delete")
    public ResponseDetails deleteTempFile() {
        return ResponseDetails.ok().put("data", deleteTempFileTasks.handMovementDeleteFile());
    }
}
