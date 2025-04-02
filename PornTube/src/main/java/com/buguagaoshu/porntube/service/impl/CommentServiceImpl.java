package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.enums.CommentType;
import com.buguagaoshu.porntube.enums.SortType;
import com.buguagaoshu.porntube.model.CustomPage;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.UserService;
import com.buguagaoshu.porntube.utils.IpUtil;
import com.buguagaoshu.porntube.utils.JwtUtil;
import com.buguagaoshu.porntube.vo.CommentVo;
import com.buguagaoshu.porntube.vo.CommentWithUserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.CommentDao;
import com.buguagaoshu.porntube.entity.CommentEntity;
import com.buguagaoshu.porntube.service.CommentService;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;


@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentDao, CommentEntity> implements CommentService {

    private final ArticleService articleService;

    private final UserService userService;

    @Autowired
    public CommentServiceImpl(ArticleService articleService, UserService userService) {
        this.articleService = articleService;
        this.userService = userService;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params, HttpServletRequest request) {
        Long article = Long.parseLong((String) params.get("article"));
        Integer type = Integer.parseInt((String) params.get("type"));
        Integer sort = Integer.parseInt((String) params.get("sort"));
        if (type == CommentType.SECOND_COMMENT) {
            Long fatherId = Long.parseLong((String) params.get("fatherId"));
            return commentList(params, article, fatherId, 2, sort);
        } else {
            return commentList(params, article, 0, 1, sort);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentEntity saveComment(CommentVo commentVo, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        ArticleEntity articleEntity = articleService.getById(commentVo.getArticleId());
        if (articleEntity == null) {
            return null;
        }
        // 判断是否有权评论
        if (articleEntity.getExamineStatus() == 0
                || articleEntity.getExamineStatus() == 2
                || articleEntity.getStatus() == 1) {
            return null;
        }
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.initComment();
        // 设置评论类型及评论ID
        commentEntity.setUserId(userId);
        commentEntity.setArticleId(articleEntity.getId());
        // 一级评论处理
        if (commentVo.getType() == 1) {
            commentEntity.setParentCommentId(0L);
            commentEntity.setParentUserId(0L);
            commentEntity.setType(1);
        } else {
            // 二级评论处理
            if (commentVo.getParentCommentId() == null) {
                return null;
            }
            // 获取父评论信息
            CommentEntity fatherComment = this.getById(commentVo.getParentCommentId());
            if (fatherComment == null) {
                return null;
            }
            if (fatherComment.getStatus() == 1) {
                return null;
            }
            // 判断是否是同一ID下的评论
            if (!fatherComment.getArticleId().equals(articleEntity.getId())) {
                return null;
            }
            // 补全父评论信息
            if (fatherComment.getCommentId() == null) {
                commentEntity.setCommentId(fatherComment.getId());
            } else {
                // 二级评论下的评论
                commentEntity.setCommentId(fatherComment.getCommentId());
            }

            commentEntity.setParentCommentId(fatherComment.getId());
            commentEntity.setParentUserId(fatherComment.getUserId());
            commentEntity.setType(2);


            if (!commentEntity.getCommentId().equals(commentEntity.getParentCommentId())) {
                this.addCount("comment_count", commentEntity.getCommentId(), 1L);
            }

            this.addCount("comment_count", fatherComment.getId(), 1L);
        }

        commentEntity.setIp(IpUtil.getIpAddr(request));
        commentEntity.setUa(IpUtil.getUa(request));
        commentEntity.setComment(commentVo.getComment());
        // TODO 保存地址信息，包括视频等
        // 保存评论
        this.save(commentEntity);
        articleService.addCount("comment_count", articleEntity.getId(), 1L);
        // TODO 通知作者

        return commentEntity;
    }

    @Override
    public PageUtils commentList(Map<String, Object> params, long articleId, long fatherId, int type, Integer sort) {
        QueryWrapper<CommentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.eq("type", type);
        wrapper.eq("status", 0);
        if (type == CommentType.SECOND_COMMENT) {
            wrapper.eq("comment_id", fatherId);
        }
        if (sort != null) {
            if (sort == SortType.TIME_DESC) {
                wrapper.orderByDesc("create_time");
            } else if (sort == SortType.TOP_COMMENT) {
                wrapper.orderByDesc("comment_count");
            }
        }
        IPage<CommentEntity> page = this.page(
                new Query<CommentEntity>().getPage(params),
                wrapper
        );
        if (page.getRecords() == null || page.getRecords().size() == 0) {
            return new PageUtils(page);
        }
        Set<Long> userSet = page.getRecords().stream().map(CommentEntity::getUserId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userService.listByIds(userSet).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<CommentWithUserVo> commentWithUserVoList = new ArrayList<>();

        for (CommentEntity comment : page.getRecords()) {
            CommentWithUserVo commentWithUserVo = new CommentWithUserVo();
            BeanUtils.copyProperties(comment, commentWithUserVo);
            BeanUtils.copyProperties(userMap.get(comment.getUserId()), commentWithUserVo);
            commentWithUserVo.setId(comment.getId());
            commentWithUserVoList.add(commentWithUserVo);

        }

        IPage<CommentWithUserVo> commentWithUserVoIPage = new CustomPage<>(
                commentWithUserVoList,
                page.getTotal(),
                page.getSize(),
                page.getCurrent()
        );
        return new PageUtils(commentWithUserVoIPage);
    }

    @Override
    public void addCount(String col, long commentId, long count) {
        this.baseMapper.addCount(col, commentId, count);
    }

}