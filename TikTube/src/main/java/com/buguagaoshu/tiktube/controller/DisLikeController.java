package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.DislikeTableEntity;
import com.buguagaoshu.tiktube.service.DislikeTableService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.valid.ListValue;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-13
 */
@RestController
@RequestMapping("/api")
public class DisLikeController {

    final DislikeTableService dislikeTableService;

    @Autowired
    public DisLikeController(DislikeTableService dislikeTableService) {
        this.dislikeTableService = dislikeTableService;
    }

    /**
     * 点踩接口
     *
     * @param dislikeObjId 点踩对象ID
     * @param type         点踩类型 0:视频/图片/文章 1:评论
     * @param request      HTTP请求
     * @return 操作结果
     */
    @PostMapping("/dislike/toggle")
    public ResponseDetails toggleDislike(@RequestParam Long dislikeObjId,
                                         @RequestParam @ListValue(value = {0, 1}) Integer type,
                                         HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", dislikeTableService.toggleDislike(dislikeObjId, type, userId));
    }

    @GetMapping("/dislike/status")
    public ResponseDetails getDislike(@RequestParam Long dislikeObjId,
                                      @RequestParam @ListValue(value = {0, 1}) Integer type,
                                      HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);

        DislikeTableEntity dislikeTableEntity = dislikeTableService.checkDislike(dislikeObjId, type, userId);
        if (dislikeTableEntity != null) {
            return ResponseDetails.ok().put("data", true);
        }
        return ResponseDetails.ok().put("data", false);
    }
}
