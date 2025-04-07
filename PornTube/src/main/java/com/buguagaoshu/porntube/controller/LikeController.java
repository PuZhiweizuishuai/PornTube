package com.buguagaoshu.porntube.controller;


import com.buguagaoshu.porntube.entity.LikeTableEntity;

import com.buguagaoshu.porntube.service.LikeTableService;
import com.buguagaoshu.porntube.utils.JwtUtil;

import com.buguagaoshu.porntube.vo.ResponseDetails;
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
     * @param likeObjId 点赞对象ID
     * @param type 点赞类型 0:视频/图片/文章 1:评论
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/like/toggle")
    public ResponseDetails toggleLike(@RequestParam Long likeObjId, @RequestParam Integer type, HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", likeTableService.toggleLike(likeObjId, type, userId));
    }


    @GetMapping("/like/status")
    public ResponseDetails getLike(@RequestParam Long likeObjId, @RequestParam Integer type, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        if (type == 0 || type == 1) {
            LikeTableEntity likeTableEntity = likeTableService.checkLike(likeObjId, type, userId);
            if (likeTableEntity != null) {
                return ResponseDetails.ok().put("data", true);
            }
            return ResponseDetails.ok().put("data", false);

        }
        return ResponseDetails.ok().put("data", false);
    }
}
