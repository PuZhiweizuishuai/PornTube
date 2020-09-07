package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 19:20
 */
@RestController
public class CategoryController {

    private final CategoryCache categoryCache;

    @Autowired
    public CategoryController(CategoryCache categoryCache) {
        this.categoryCache = categoryCache;
    }

    @GetMapping("/api/category/list")
    public ResponseDetails list() {
        return ResponseDetails.ok().put("data", categoryCache.getCategoryEntities());
    }
}
