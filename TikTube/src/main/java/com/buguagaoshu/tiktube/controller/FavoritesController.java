package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.FavoritesTableEntity;
import com.buguagaoshu.tiktube.service.FavoritesTableService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-08
 */
@RestController
@RequestMapping("/api")
public class FavoritesController {
    final FavoritesTableService favoritesTableService;

    @Autowired
    public FavoritesController(FavoritesTableService favoritesTableService) {
        this.favoritesTableService = favoritesTableService;
    }

    @PostMapping("/favorites/toggle")
    public ResponseDetails toggleFavorites(@RequestBody FavoritesTableEntity favoritesTableEntity, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", favoritesTableService.toggleFavorites(favoritesTableEntity, userId));
    }


    @GetMapping("/favorites/check")
    public ResponseDetails checkFavorites(@RequestParam Long articleId, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", favoritesTableService.checkFavorites(articleId, userId));
    }


    @GetMapping("/favorites/user/list")
    public ResponseDetails userFavoriteList(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", favoritesTableService.userFavorites(params, userId));
    }
}
