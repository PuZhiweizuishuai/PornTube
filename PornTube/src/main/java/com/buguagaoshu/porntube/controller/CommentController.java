package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.entity.CommentEntity;
import com.buguagaoshu.porntube.service.CommentService;
import com.buguagaoshu.porntube.vo.CommentVo;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-08-25 15:57
 */
@RestController
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/comment/save")
    public ResponseDetails saveComment(@Valid @RequestBody CommentVo commentVo,
                                       HttpServletRequest request) {
        CommentEntity comment = commentService.saveComment(commentVo, request);
        if (comment == null) {
            return ResponseDetails.ok(0, "所评论的帖子可能被删除被锁定或没有评论权限！");
        }
        return ResponseDetails.ok().put("data", comment);
    }


    @GetMapping("/api/comment/list")
    public ResponseDetails commentList(@RequestParam Map<String, Object> params,
                                       HttpServletRequest request) {
        return ResponseDetails.ok().put("data", commentService.queryPage(params, request));
    }
}
