package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.FollowEntity;
import com.buguagaoshu.tiktube.service.FollowService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
@RestController
@RequestMapping("/api/follow")
public class FollowController {
    final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/add")
    public ResponseDetails addFollow(@RequestBody FollowEntity follow, HttpServletRequest request) {
        return ResponseDetails.ok()
                .put("data", followService.addFollow(follow, JwtUtil.getUserId(request)));
    }

    @PostMapping("/delete")
    public ResponseDetails deleteFollow(@RequestBody FollowEntity follow, HttpServletRequest request) {
        return ResponseDetails.ok()
                .put("data", followService.deleteFollow(follow, JwtUtil.getUserId(request)));
    }


    @PostMapping("/remove")
    public ResponseDetails removeFans(@RequestBody FollowEntity follow, HttpServletRequest request) {
        return ResponseDetails.ok()
                .put("data", followService.removeFans(follow, JwtUtil.getUserId(request)));
    }


    @GetMapping("/check")
    public ResponseDetails checkFollow(@RequestParam Long followId, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", followService.checkFollow(followId, JwtUtil.getUserId(request)));
    }

    @GetMapping("/follow/article")
    public ResponseDetails followArticle(@RequestParam Map<String, Object> params,
                                         HttpServletRequest request) {
        return ResponseDetails.ok().put("data", followService.queryFollowArticle(params, JwtUtil.getUserId(request)));
    }

    /**
     * 关注列表
     * */
    @GetMapping("/follow")
    public ResponseDetails followList(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", followService.queryFollow(params, JwtUtil.getUserId(request)));
    }

    /**
     * 粉丝列表
     * */
    @GetMapping("/fans")
    public ResponseDetails fansList(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        return ResponseDetails.ok().put("data", followService.queryFansList(params, JwtUtil.getUserId(request)));
    }
}
