package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.entity.CategoryEntity;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 19:20
 */
@RestController
public class CategoryController {

    private final CategoryCache categoryCache;

    private final ArticleService articleService;

    @Autowired
    public CategoryController(CategoryCache categoryCache, ArticleService articleService) {
        this.categoryCache = categoryCache;
        this.articleService = articleService;
    }

    @GetMapping("/api/category/list")
    public ResponseDetails list() {
        return ResponseDetails.ok().put("data", categoryCache.getCategoryEntities());
    }


    @GetMapping("/api/category/tree")
    public ResponseDetails tree(@RequestParam(required = false) Integer category) {
        if (category == null) {
            return ResponseDetails.ok().put("data", categoryCache.getCategoryMapWithChildren());
        }
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(category);
        if (categoryEntity.getFatherId() == 0) {
            return ResponseDetails.ok().put("data", categoryCache.getCategoryMapWithChildren().get(category));
        } else {
            return ResponseDetails.ok().put("data", categoryCache.getCategoryMapWithChildren().get(categoryEntity.getFatherId()));
        }
    }


    @GetMapping("/api/article/category/{id}")
    public ResponseDetails nowCategory(@RequestParam Map<String, Object> params,
                                       @PathVariable(value = "id") Integer id) {

        return ResponseDetails.ok().put("data", articleService.nowCategory(params, id));
    }
}
