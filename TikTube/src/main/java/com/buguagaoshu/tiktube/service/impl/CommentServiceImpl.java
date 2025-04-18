package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.enums.CommentType;
import com.buguagaoshu.tiktube.enums.SortType;
import com.buguagaoshu.tiktube.model.CustomPage;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.CommentVo;
import com.buguagaoshu.tiktube.vo.CommentWithUserVo;
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
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.CommentDao;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.service.CommentService;
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
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
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
            commentWithUserVo.setCreateTime(comment.getCreateTime());
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
    public PageUtils getAllComment(Map<String, Object> params) {
        QueryWrapper<CommentEntity> wrapper = new QueryWrapper<>();
        String userId = (String) params.get("userId");
        String articleId = (String) params.get("articleId");
        String status = (String) params.get("status");

        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (articleId != null) {
            wrapper.eq("article_id", articleId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("update_time");
        IPage<CommentEntity> page = this.page(
                new Query<CommentEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCommentStatus(long id) {
        CommentEntity comment = this.getById(id);
        if (comment == null) {
            return false;
        }
        comment.setUpdateTime(System.currentTimeMillis());
        // 正常状态
        if (comment.getStatus().equals(ArticleStatusEnum.NORMAL.getCode())) {
            // 删除评论
            comment.setStatus(ArticleStatusEnum.DELETE.getCode());
            articleService.addCount("comment_count", comment.getArticleId(), -1L);
            commentCountController(comment, -1L);
        } else {
            // 恢复评论
            comment.setStatus(ArticleStatusEnum.NORMAL.getCode());
            articleService.addCount("comment_count", comment.getArticleId(), 1L);
            commentCountController(comment, 1L);
        }
        this.updateById(comment);

        return true;
    }

    public void commentCountController(CommentEntity comment, long count) {
        if (comment.getType().equals(CommentType.SECOND_COMMENT)) {
            // 更新父级评论数量与目标评论数量
            if (comment.getParentCommentId().equals(comment.getCommentId())) {
                addCount("comment_count", comment.getCommentId(), count);
            } else {
                addCount("comment_count", comment.getCommentId(), count);
                addCount("comment_count", comment.getParentCommentId(), count);
            }
        }
    }


    @Override
    public void addCount(String col, long commentId, long count) {
        this.baseMapper.addCount(col, commentId, count);
    }

}