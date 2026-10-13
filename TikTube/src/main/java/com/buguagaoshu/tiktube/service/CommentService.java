package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.vo.CommentVo;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface CommentService extends IService<CommentEntity> {

    PageUtils queryPage(Map<String, Object> params, HttpServletRequest request);

    /**
     * 保存评论
     * */
    CommentEntity saveComment(CommentVo commentVo, HttpServletRequest request);


    /**
     * 获取稿件评论列表
     *
     * @param userId 当前登录用户，用于批量带出点赞状态；未登录传 null
     * */
    PageUtils commentList(Map<String, Object> params, long articleId, long fatherId, int type, Integer sort, Long userId);


    /**
     * 展示所有评论列表
     * */
    PageUtils getAllComment(Map<String, Object> params);


    /**
     * 切换评论状态
     * */
    boolean toggleCommentStatus(long id);


    boolean hasUserCommentInArticle(long id, long userId);



    void addCount(String col, long commentId, long count);


    /**
     * 发送评论通知
     * */
    void send(CommentEntity commentEntity,
              CommentEntity fatherComment,
              ArticleEntity articleEntity);
}

