package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.model.CustomPage;
import com.buguagaoshu.tiktube.pipe.AiExaminePipe;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.*;
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
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;

import com.buguagaoshu.tiktube.dao.CommentDao;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.service.CommentService;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;


@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentDao, CommentEntity> implements CommentService {

    private final ArticleService articleService;

    private final UserService userService;

    private final NotificationService notificationService;

    private final WebSettingCache webSettingCache;

    private final CountRecorder countRecorder;

    private final IpUtil ipUtil;

    private final AiExaminePipe aiExaminePipe;

    @Autowired
    public CommentServiceImpl(ArticleService articleService,
                              UserService userService,
                              NotificationService notificationService,
                              WebSettingCache webSettingCache,
                              CountRecorder countRecorder,
                              IpUtil ipUtil, AiExaminePipe aiExaminePipe) {
        this.articleService = articleService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.webSettingCache = webSettingCache;
        this.countRecorder = countRecorder;
        this.ipUtil = ipUtil;
        this.aiExaminePipe = aiExaminePipe;
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
        CommentEntity fatherComment = null;
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
            fatherComment = this.getById(commentVo.getParentCommentId());
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
                countRecorder.recordComment(commentEntity.getCommentId(), 1L);
                //this.addCount("comment_count", commentEntity.getCommentId(), 1L);
            }
            countRecorder.recordComment(fatherComment.getId(), 1L);
            // this.addCount("comment_count", fatherComment.getId(), 1L);
        }
        String ip = ipUtil.getIpAddr(request);
        commentEntity.setIp(ip);
        commentEntity.setUa(ipUtil.getUa(request));
        commentEntity.setCity(ipUtil.getCity(ip));
        commentEntity.setComment(commentVo.getComment());
        // 如果开启评论审核
        if (webSettingCache.getWebConfigData().getOpenCommentExam()) {
            commentEntity.setStatus(TypeCode.EXAM);
            // 审核通过后再展示
            this.save(commentEntity);
            // 是否开启AI审核
            if (webSettingCache.getWebConfigData().getOpenAIConfig()) {
                aiExaminePipe.submitComment(commentEntity);
            }
            countRecorder.recordArticleComment(articleEntity.getId(), 1L);
            return commentEntity;
        } else {
            commentEntity.setStatus(TypeCode.NORMAL);
            // TODO 保存地址信息，包括视频等
            // 保存评论
            this.save(commentEntity);
            countRecorder.recordArticleComment(articleEntity.getId(), 1L);
            send(commentEntity, fatherComment, articleEntity);
            return commentEntity;
        }
    }

    @Override
    public void send(CommentEntity commentEntity,
                     CommentEntity fatherComment,
                     ArticleEntity articleEntity) {
        // 一级评论只通知稿件作者，二级评论只通知评论作者
        if (commentEntity.getType() == 1) {
            notificationService.sendNotification(
                    commentEntity.getUserId(),
                    articleEntity.getUserId(),
                    articleEntity.getId(),
                    articleEntity.getId(),
                    commentEntity.getId(),
                    "稿件《" + articleEntity.getTitle() + "》收到新评论",
                    "",
                    commentEntity.getComment(),
                    NotificationType.REPLY_POST
            );
        } else {
            notificationService.sendNotification(
                    commentEntity.getUserId(),
                    commentEntity.getParentUserId(),
                    // 二级评论中回复的评论ID
                    commentEntity.getParentCommentId(),
                    articleEntity.getId(),
                    // 发送出的评论ID
                    commentEntity.getId(),
                    "你在稿件《" + articleEntity.getTitle() + "》下的评论：" + MyStringUtils.extractString(fatherComment.getComment(), 50) + "收到新回复了",
                    "",
                    commentEntity.getComment(),
                    NotificationType.REPLY_COMMENT
            );
        }
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
            } else if (sort == SortType.TOP_START) {
                wrapper.orderByDesc("like_count");
            }
        }
        IPage<CommentEntity> page = this.page(
                new Query<CommentEntity>().getPage(params),
                wrapper
        );
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return new PageUtils(page);
        }
        Set<Long> userSet = page.getRecords().stream().map(CommentEntity::getUserId).collect(Collectors.toSet());
        Set<Long> pUserSet = page.getRecords().stream().map(CommentEntity::getParentUserId).collect(Collectors.toSet());
        userSet.addAll(pUserSet);
        Map<Long, UserEntity> userMap = userService.listByIds(userSet).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<CommentWithUserVo> commentWithUserVoList = new ArrayList<>();

        for (CommentEntity comment : page.getRecords()) {

            countRecorder.syncCommentCount(comment);

            CommentWithUserVo commentWithUserVo = new CommentWithUserVo();
            BeanUtils.copyProperties(comment, commentWithUserVo);
            BeanUtils.copyProperties(userMap.get(comment.getUserId()), commentWithUserVo);
            commentWithUserVo.setId(comment.getId());
            commentWithUserVo.setCreateTime(comment.getCreateTime());
            UserEntity p = userMap.get(comment.getParentUserId());
            if (p != null) {
                commentWithUserVo.setParentUserName(p.getUsername());
            }
            commentWithUserVoList.add(commentWithUserVo);

        }

        return new PageUtils(commentWithUserVoList, page.getTotal(), page.getSize(), page.getCurrent());
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
        // 数据同步
        page.getRecords().forEach(countRecorder::syncCommentCount);

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

        if (comment.getStatus().equals(ArticleStatusEnum.DELETE.getCode())) {
            // 恢复评论
            comment.setStatus(ArticleStatusEnum.NORMAL.getCode());
            articleService.addCount("comment_count", comment.getArticleId(), 1L);
            commentCountController(comment, 1L);
        } else {
            // 删除评论
            comment.setStatus(ArticleStatusEnum.DELETE.getCode());
            articleService.addCount("comment_count", comment.getArticleId(), -1L);
            commentCountController(comment, -1L);
        }
        this.updateById(comment);

        return true;
    }

    @Override
    public boolean hasUserCommentInArticle(long id, long userId) {
        QueryWrapper<CommentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", id);
        wrapper.eq("user_id", userId);
        List<CommentEntity> list = this.list(wrapper);
        return list != null && !list.isEmpty();
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
        // col 会被拼进 update 语句，只允许计数列白名单
        this.baseMapper.addCount(SQLFilter.checkedCountColumn(col), commentId, count);
    }

}