package com.buguagaoshu.tiktube.controller;


import com.buguagaoshu.tiktube.entity.LikeTableEntity;

import com.buguagaoshu.tiktube.service.LikeTableService;
import com.buguagaoshu.tiktube.utils.JwtUtil;

import com.buguagaoshu.tiktube.valid.ListValue;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-07
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class LikeController {
    final LikeTableService likeTableService;

    @Autowired
    public LikeController(LikeTableService likeTableService) {
        this.likeTableService = likeTableService;
    }

    /**
     * 点赞接口
     *
     * @param likeObjId 点赞对象ID
     * @param type      点赞类型 0:视频/图片/文章 1:评论
     * @param request   HTTP请求
     * @return 操作结果
     */
    @PostMapping("/like/toggle")
    public ResponseDetails toggleLike(@RequestParam Long likeObjId,
                                      @RequestParam @ListValue(value = {0, 1}) Integer type,
                                      HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", likeTableService.toggleLike(likeObjId, type, userId));
    }


    @GetMapping("/like/status")
    public ResponseDetails getLike(@RequestParam Long likeObjId,
                                   @RequestParam @ListValue(value = {0, 1}) Integer type,
                                   HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);

        LikeTableEntity likeTableEntity = likeTableService.checkLike(likeObjId, type, userId);
        if (likeTableEntity != null) {
            return ResponseDetails.ok().put("data", true);
        }
        return ResponseDetails.ok().put("data", false);
    }
}
