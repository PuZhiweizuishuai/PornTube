package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.dao.OpinionDao;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.entity.OpinionEntity;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.enums.TypeCode;
import com.buguagaoshu.tiktube.model.OpinionOtherInfo;
import com.buguagaoshu.tiktube.utils.MyStringUtils;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-07
 */
@Service
@Slf4j
public class OpinionService extends ServiceImpl<OpinionDao, OpinionEntity> {

    private final NotificationService notificationService;

    private final ArticleService articleService;


    private final CommentService commentService;

    private final DanmakuService danmakuService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OpinionService(NotificationService notificationService,
                          ArticleService articleService,
                          CommentService commentService,
                          DanmakuService danmakuService) {
        this.notificationService = notificationService;
        this.articleService = articleService;
        this.commentService = commentService;
        this.danmakuService = danmakuService;
    }

    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<OpinionEntity> wrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if (status != null) {
            wrapper.eq("status", status);
        }
        String type = (String) params.get("type");
        if (type != null) {
            wrapper.eq("type", type);
        }
        String target = (String) params.get("target");
        if (target != null) {
            wrapper.eq("target_id", target);
        }
        wrapper.orderByDesc("create_time");
        IPage<OpinionEntity> page = this.page(
                new Query<OpinionEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    public OpinionEntity info(Long id, long userId) {
        OpinionEntity opinion = this.getById(id);
        if (opinion == null) {
            return null;
        }
        if (opinion.getUserId().equals(userId)) {
            opinion.setOtherInfo("");
            return opinion;
        }
        return null;
    }

    public OpinionEntity findOpinion(long targetId, int type, long userId) {
        if (type == TypeCode.OPINION_TYPE_FEEDBACK) {
            return null;
        }
        QueryWrapper<OpinionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id", targetId);
        wrapper.eq("type", type);
        wrapper.eq("user_id", userId);
        return this.getOne(wrapper);
    }


    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum save(OpinionEntity opinionEntity, long userId) {
        // 处理申诉
        if (isAppealType(opinionEntity.getType())) {
            return handleAppeal(opinionEntity, userId);
        }
        
        OpinionOtherInfo other = getOtherInfo(opinionEntity);
        if (other == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        OpinionEntity opinion = findOpinion(opinionEntity.getTargetId(), opinionEntity.getType(), userId);
        if (opinion != null) {
            return ReturnCodeEnum.REPEATED_REPORTING;
        }
        opinionEntity.setUserId(userId);
        opinionEntity.setStatus(TypeCode.OPINION_STATUS_UNTREATED);
        opinionEntity.setCreateTime(System.currentTimeMillis());
        opinionEntity.setOtherInfo(other.getOtherInfoJson());
        this.save(opinionEntity);

        String title = "系统已收到您的举报";

        if (opinionEntity.getType().equals(TypeCode.OPINION_TYPE_FEEDBACK)) {
            other.setLink(opinionEntity.getId().toString());
            title = "感谢您的意见反馈";
        }

        notificationService.sendNotification(
                0,
                userId,
                opinionEntity.getId(),
                opinionEntity.getTargetId(),
                opinionEntity.getTargetId(),
                title,
                other.getLink(),
                other.getContent(),
                NotificationType.ACCUSATION
        );
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 判断是否为申诉类型
     * @param type 类型代码
     * @return 是否为申诉类型
     */
    private boolean isAppealType(int type) {
        return type == TypeCode.APPEAL_TYPE_ARTICLE 
            || type == TypeCode.APPEAL_TYPE_COMMENT 
            || type == TypeCode.APPEAL_TYPE_DANMAKU;
    }

    /**
     * 处理申诉请求
     * @param opinionEntity 申诉实体
     * @param userId 用户ID
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum handleAppeal(OpinionEntity opinionEntity, long userId) {
        try {
            // 根据申诉类型获取对应实体
            if (opinionEntity.getType() == TypeCode.APPEAL_TYPE_ARTICLE) {
                // 处理稿件申诉
                ArticleEntity article = articleService.getById(opinionEntity.getTargetId());
                if (article == null || !article.getUserId().equals(userId)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 检查稿件状态是否为删除状态
                if (!article.getStatus().equals(TypeCode.DELETE)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 设置申诉信息
                OpinionOtherInfo opinionOtherInfo = new OpinionOtherInfo();
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(article));
                opinionOtherInfo.setContent("您对稿件：" + article.getTitle() + " 的申诉已提交，我们将尽快处理");
                opinionOtherInfo.setLink(article.getId().toString());
                
                // 保存申诉信息
                opinionEntity.setUserId(userId);
                opinionEntity.setStatus(TypeCode.OPINION_STATUS_UNTREATED);
                opinionEntity.setCreateTime(System.currentTimeMillis());
                opinionEntity.setOtherInfo(opinionOtherInfo.getOtherInfoJson());
                this.save(opinionEntity);
                
                // 发送通知
                notificationService.sendNotification(
                        0,
                        userId,
                        opinionEntity.getId(),
                        opinionEntity.getTargetId(),
                        opinionEntity.getTargetId(),
                        "系统已收到您的申诉",
                        opinionOtherInfo.getLink(),
                        opinionOtherInfo.getContent(),
                        NotificationType.ACCUSATION
                );
                
                return ReturnCodeEnum.SUCCESS;
                
            } else if (opinionEntity.getType() == TypeCode.APPEAL_TYPE_COMMENT) {
                // 处理评论申诉
                CommentEntity comment = commentService.getById(opinionEntity.getTargetId());
                if (comment == null || !comment.getUserId().equals(userId)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 检查评论状态是否为删除状态
                if (!comment.getStatus().equals(TypeCode.DELETE)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 设置申诉信息
                OpinionOtherInfo opinionOtherInfo = new OpinionOtherInfo();
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(comment));
                opinionOtherInfo.setContent("您对评论：" + MyStringUtils.extractString(comment.getComment(), 50) + " 的申诉已提交，我们将尽快处理");
                opinionOtherInfo.setLink(comment.getId().toString());
                
                // 保存申诉信息
                opinionEntity.setUserId(userId);
                opinionEntity.setStatus(TypeCode.OPINION_STATUS_UNTREATED);
                opinionEntity.setCreateTime(System.currentTimeMillis());
                opinionEntity.setOtherInfo(opinionOtherInfo.getOtherInfoJson());
                this.save(opinionEntity);
                
                // 发送通知
                notificationService.sendNotification(
                        0,
                        userId,
                        opinionEntity.getId(),
                        opinionEntity.getTargetId(),
                        opinionEntity.getTargetId(),
                        "系统已收到您的申诉",
                        opinionOtherInfo.getLink(),
                        opinionOtherInfo.getContent(),
                        NotificationType.ACCUSATION
                );
                
                return ReturnCodeEnum.SUCCESS;
                
            } else if (opinionEntity.getType() == TypeCode.APPEAL_TYPE_DANMAKU) {
                // 处理弹幕申诉
                DanmakuEntity danmaku = danmakuService.getById(opinionEntity.getTargetId());
                if (danmaku == null || !danmaku.getAuthor().equals(userId)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 检查弹幕状态是否为删除状态
                if (!danmaku.getStatus().equals(TypeCode.DELETE)) {
                    return ReturnCodeEnum.NOO_FOUND;
                }
                
                // 设置申诉信息
                OpinionOtherInfo opinionOtherInfo = new OpinionOtherInfo();
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(danmaku));
                opinionOtherInfo.setContent("您对弹幕：" + danmaku.getText() + " 的申诉已提交，我们将尽快处理");
                opinionOtherInfo.setLink(danmaku.getId().toString());
                
                // 保存申诉信息
                opinionEntity.setUserId(userId);
                opinionEntity.setStatus(TypeCode.OPINION_STATUS_UNTREATED);
                opinionEntity.setCreateTime(System.currentTimeMillis());
                opinionEntity.setOtherInfo(opinionOtherInfo.getOtherInfoJson());
                this.save(opinionEntity);
                
                // 发送通知
                notificationService.sendNotification(
                        0,
                        userId,
                        opinionEntity.getId(),
                        opinionEntity.getTargetId(),
                        opinionEntity.getTargetId(),
                        "系统已收到您的申诉",
                        opinionOtherInfo.getLink(),
                        opinionOtherInfo.getContent(),
                        NotificationType.ACCUSATION
                );
                
                return ReturnCodeEnum.SUCCESS;
            }
            
            return ReturnCodeEnum.NOO_FOUND;
        } catch (Exception e) {
            log.error("处理申诉异常： {}", e.getMessage());
            return ReturnCodeEnum.NOO_FOUND;
        }
    }

    /**
     * 处理申诉结果
     * @param opinionEntity 申诉实体
     * @param userId 处理人ID
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum handleAppealResult(OpinionEntity opinionEntity, long userId) {
        try {
            OpinionEntity byId = this.getById(opinionEntity.getId());
            if (byId == null || !isAppealType(byId.getType())) {
                return ReturnCodeEnum.NOO_FOUND;
            }
            
            // 如果申诉通过，恢复相应内容的状态
            if (opinionEntity.getAppealStatus().equals(TypeCode.NORMAL)) {
                if (byId.getType() == TypeCode.APPEAL_TYPE_ARTICLE) {
                    // 恢复稿件状态
                    ArticleEntity article = articleService.getById(byId.getTargetId());
                    if (article == null) {
                        return ReturnCodeEnum.NOO_FOUND;
                    }
                    article.setStatus(TypeCode.NORMAL);
                    articleService.updateById(article);
                    
                } else if (byId.getType() == TypeCode.APPEAL_TYPE_COMMENT) {
                    // 恢复评论状态
                    CommentEntity comment = commentService.getById(byId.getTargetId());
                    if (comment == null) {
                        return ReturnCodeEnum.NOO_FOUND;
                    }
                    comment.setStatus(TypeCode.NORMAL);
                    commentService.updateById(comment);
                    
                } else if (byId.getType() == TypeCode.APPEAL_TYPE_DANMAKU) {
                    // 恢复弹幕状态
                    DanmakuEntity danmaku = danmakuService.getById(byId.getTargetId());
                    if (danmaku == null) {
                        return ReturnCodeEnum.NOO_FOUND;
                    }
                    danmaku.setStatus(TypeCode.NORMAL);
                    danmakuService.updateById(danmaku);
                }
            }
            
            // 更新申诉处理结果
            byId.setOpinionUser(userId);
            byId.setOpinion(opinionEntity.getOpinion());
            byId.setStatus(opinionEntity.getStatus());
            byId.setOpinionTime(System.currentTimeMillis());
            this.updateById(byId);
            
            // 发送申诉结果通知
            String title = "申诉结果";
            String content = opinionEntity.getStatus().equals(TypeCode.NORMAL) ?
                    "您的申诉已通过，相关内容已恢复" : "您的申诉未通过，详情请查看管理员反馈";
            
            notificationService.sendNotification(
                    userId,
                    byId.getUserId(),
                    byId.getId(),
                    byId.getTargetId(),
                    byId.getTargetId(),
                    title,
                    byId.getId().toString(),
                    content + "：" + byId.getOpinion(),
                    NotificationType.ACCUSATION
            );
            
            return ReturnCodeEnum.SUCCESS;
        } catch (Exception e) {
            log.error("处理申诉结果异常： {}", e.getMessage());
            return ReturnCodeEnum.NOO_FOUND;
        }
    }

    public OpinionOtherInfo getOtherInfo(OpinionEntity opinionEntity) {
        try {
            OpinionOtherInfo opinionOtherInfo = new OpinionOtherInfo();
            if (opinionEntity.getType().equals(TypeCode.OPINION_TYPE_ARTICLE)) {
                ArticleEntity article = articleService.getById(opinionEntity.getTargetId());
                if (article == null) {
                    return null;
                }
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(article));
                opinionOtherInfo.setContent("你举报的稿件：" + article.getTitle() + "，我们已经收到，稍后管理员将给你反馈");
                opinionOtherInfo.setLink(article.getId().toString());
                return opinionOtherInfo;
            } else if (opinionEntity.getType().equals(TypeCode.OPINION_TYPE_COMMENT)) {
                CommentEntity comment = commentService.getById(opinionEntity.getTargetId());
                if (comment == null) {
                    return null;
                }
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(comment));
                opinionOtherInfo.setContent("你举报的评论：" + MyStringUtils.extractString(comment.getComment(), 50) + "，我们已经收到，稍后管理员将给你反馈");
                opinionOtherInfo.setLink(comment.getId().toString());
                return opinionOtherInfo;
            } else if (opinionEntity.getType().equals(TypeCode.OPINION_TYPE_DANMAKU)) {
                DanmakuEntity danmaku = danmakuService.getById(opinionEntity.getTargetId());
                if (danmaku == null) {
                    return null;
                }
                opinionOtherInfo.setOtherInfoJson(objectMapper.writeValueAsString(danmaku));
                opinionOtherInfo.setContent("你举报的弹幕：" + danmaku.getText() + "，我们已经收到，稍后管理员将给你反馈");
                opinionOtherInfo.setLink(danmaku.getId().toString());
                return opinionOtherInfo;
            } else if (opinionEntity.getType().equals(TypeCode.OPINION_TYPE_FEEDBACK)) {
                opinionOtherInfo.setContent("感谢你的建议，我们将在稍后给您进行反馈！");
                return opinionOtherInfo;
            }
            return null;
        } catch (Exception e) {
            log.error("获取举报信息反馈其它信息异常： {}", e.getMessage());
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum acceptance(OpinionEntity opinionEntity, long userId) {
        OpinionEntity byId = this.getById(opinionEntity.getId());
        if (byId == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        
        // 如果是申诉类型，调用申诉处理方法
        if (isAppealType(byId.getType())) {
            return handleAppealResult(opinionEntity, userId);
        }
        
        byId.setOpinionUser(userId);
        byId.setOpinion(opinionEntity.getOpinion());
        byId.setStatus(opinionEntity.getStatus());
        byId.setOpinionTime(System.currentTimeMillis());
        this.updateById(byId);
        String title = "举报结果";
        if (byId.getType().equals(TypeCode.OPINION_TYPE_FEEDBACK)) {
            title = "意见反馈结果";
        }
        notificationService.sendNotification(
                userId,
                byId.getUserId(),
                byId.getId(),
                byId.getTargetId(),
                byId.getTargetId(),
                title,
                byId.getId().toString(),
                byId.getOpinion(),
                NotificationType.ACCUSATION
        );
        return ReturnCodeEnum.SUCCESS;
    }
}
