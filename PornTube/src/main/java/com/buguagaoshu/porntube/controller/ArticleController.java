package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.dto.VideoArticleDto;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 20:51
 */
@RestController
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }


    @GetMapping("/api/article/home/list")
    public ResponseDetails homeList(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", articleService.queryPage(params));
    }

    @PostMapping("/api/article/video")
    public ResponseDetails videoPost(@Valid @RequestBody VideoArticleDto videoArticleDto,
                                     HttpServletRequest request) {
        return ResponseDetails.ok(articleService.saveVideo(videoArticleDto, request));
    }


    @GetMapping("/api/article/video/{id}")
    public ResponseDetails getVideo(@PathVariable(value = "id") Long id) {
        return ResponseDetails.ok().put("data", articleService.getVideo(id));
    }

}
