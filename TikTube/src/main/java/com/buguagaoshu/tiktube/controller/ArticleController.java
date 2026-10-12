package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.cache.PlayCountRecorder;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.repository.APICurrentLimitingRepository;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 20:51
 */
@RestController
public class ArticleController {

    private final ArticleService articleService;

    private final APICurrentLimitingRepository apiCurrentLimitingRepository;

    private final HotCache hotCache;


    @Autowired
    public ArticleController(ArticleService articleService,
                             APICurrentLimitingRepository apiCurrentLimitingRepository,
                             HotCache hotCache) {
        this.articleService = articleService;
        this.apiCurrentLimitingRepository = apiCurrentLimitingRepository;
        this.hotCache = hotCache;
    }


    @GetMapping("/api/article/home/list")
    public ResponseDetails homeList(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", articleService.queryPage(params));
    }

    @GetMapping("/api/article/hot")
    public ResponseDetails hot() {
        return ResponseDetails.ok().put("data", hotCache.getHotList());
    }


    @GetMapping("/api/article/user/list/{id}")
    public ResponseDetails userList(@RequestParam Map<String, Object> params,
                                    @RequestParam(value = "type", required = false) Integer type,
                                    @PathVariable(value = "id") Long id) {
        return ResponseDetails.ok().put("data", articleService.userArticleList(params, id, type));
    }

    @PostMapping("/api/article/video")
    public ResponseDetails videoPost(@Valid @RequestBody VideoArticleDto videoArticleDto,
                                     HttpServletRequest request) {
        if (apiCurrentLimitingRepository.hasVisit(WebConstant.SIMPLE_CURRENT_LIMITING_TYPE_ARTICLE, JwtUtil.getUserId(request))) {
            return ResponseDetails.ok(articleService.saveVideo(videoArticleDto, request));
        }
        return ResponseDetails.ok(ReturnCodeEnum.COUNT_LIMIT);
    }

    @PostMapping("/api/article/video/update")
    public ResponseDetails updateVideoPost(@Valid @RequestBody VideoArticleDto videoArticleDto,
                                     HttpServletRequest request) {
        return ResponseDetails.ok(articleService.updateVideo(videoArticleDto, request));
    }


    @GetMapping("/api/article/video/{id}")
    public ResponseDetails getVideo(@PathVariable(value = "id") Long id,
                                    HttpServletRequest request) {
        ArticleViewData video = articleService.getVideo(id, request);
        // 获取推荐内容
        video.setSimilar(articleService.getRecommendationsByArticleId(video.getId(), 10));
        return ResponseDetails.ok().put("data", video);
    }

    @GetMapping("/api/article/edit/{id}")
    public ResponseDetails getEditVide(@PathVariable(value = "id") Long id,
                                       HttpServletRequest request) {
        return ResponseDetails.ok().put("data", articleService.getEditInfo(id, request));
    }


    /**
     * 创作者中心
     * */
    @GetMapping("/api/studio/article/list")
    public ResponseDetails userList(@RequestParam Map<String, Object> params, @RequestParam(value = "type", required = false) String type,HttpServletRequest request) {
        return ResponseDetails.ok().put("data", articleService.userArticleList(params, type, request));
    }

    @PostMapping("/api/studio/article/delete")
    public ResponseDetails deleteArticle(@RequestBody ArticleEntity articleEntity,
                                         HttpServletRequest request) {

        return ResponseDetails.ok().put("data", articleService.deleteArticle(articleEntity, request));
    }


    /**
     *
     * 管理员审核视频
     * */
    @GetMapping("/api/admin/article/list")
    public ResponseDetails examineVideo(@RequestParam Map<String, Object> params,
                                        HttpServletRequest request) {
        return ResponseDetails.ok().put("data", articleService.examineList(params, request));
    }

    /**
     * 恢复删除视频
     * */
    @PostMapping("/api/admin/article/restore")
    public ResponseDetails restore(@RequestBody ArticleEntity articleEntity,
                                   HttpServletRequest request) {
        return ResponseDetails.ok().put("data", articleService.restore(articleEntity, request));
    }

}
