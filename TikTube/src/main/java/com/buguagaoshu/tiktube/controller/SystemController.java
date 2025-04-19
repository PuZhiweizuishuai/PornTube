package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.schedule.DeleteTempFileTasks;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public SystemController(ArticleService articleService, DeleteTempFileTasks deleteTempFileTasks) {
        this.articleService = articleService;
        this.deleteTempFileTasks = deleteTempFileTasks;
    }

    @PostMapping("/admin/system/refresh/hot")
    public ResponseDetails refreshHot(HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        log.info("管理员：id:{} name: {} 手动刷新热门数据", user.getId(), user.getSubject());
        HotCache.hotList = articleService.hotView(WebConstant.HOT_NUM);
        return ResponseDetails.ok();
    }


    @PostMapping("/admin/system/file/delete")
    public ResponseDetails deleteTempFile() {
        return ResponseDetails.ok().put("data", deleteTempFileTasks.handMovementDeleteFile());
    }
}
