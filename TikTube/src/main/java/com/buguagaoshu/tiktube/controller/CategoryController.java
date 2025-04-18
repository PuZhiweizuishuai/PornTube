package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.cache.CategoryCache;
import com.buguagaoshu.tiktube.entity.CategoryEntity;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.CategoryService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 19:20
 */
@RestController
public class CategoryController {

    private final CategoryCache categoryCache;

    private final ArticleService articleService;

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryCache categoryCache,
                              ArticleService articleService,
                              CategoryService categoryService) {
        this.categoryCache = categoryCache;
        this.articleService = articleService;
        this.categoryService = categoryService;
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


    @PostMapping("/api/admin/category/update")
    public ResponseDetails updateCategory(@RequestBody CategoryEntity categoryEntity) {
        return ResponseDetails.ok().put("data", categoryService.updateCategory(categoryEntity));
    }


    @PostMapping("/api/admin/category/save")
    public ResponseDetails saveCategory(@RequestBody CategoryEntity categoryEntity) {
        CategoryEntity category = categoryService.saveCategory(categoryEntity);
        Map<String, Object> info = new HashMap<>();
        info.put("info", "添加成功！");
        info.put("data", category);
        return ResponseDetails.ok().put("data", info);
    }


    @PostMapping("/api/admin/category/delete")
    public ResponseDetails deleteCategory(@RequestBody CategoryEntity categoryEntity) {
        return ResponseDetails.ok().put("data", categoryService.deleteCategory(categoryEntity.getId()));
    }
}
