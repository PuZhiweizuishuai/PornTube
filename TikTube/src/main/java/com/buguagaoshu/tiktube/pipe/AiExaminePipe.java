package com.buguagaoshu.tiktube.pipe;

import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.dao.CommentDao;
import com.buguagaoshu.tiktube.dao.DanmakuDao;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.enums.TypeCode;
import com.buguagaoshu.tiktube.model.AiExamineCommentAndDanmakuResult;
import com.buguagaoshu.tiktube.service.AIConfigServer;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.utils.MyStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-17
 * AI 审核管道
 * TODO 实现开启 Redis 后采用 Redis 的发布订阅模式实现此功能
 */
@Slf4j
@Component
public class AiExaminePipe {

    private final AIConfigServer aiConfigServer;
    private final CommentDao commentDao;
    private final DanmakuDao danmakuDao;
    private final NotificationService notificationService;

    // 记录 TOKEN 消耗
    private final CountRecorder countRecorder;

    // 评论审核队列
    private final BlockingQueue<CommentEntity> commentQueue = new LinkedBlockingQueue<>();
    // 弹幕审核队列
    private final BlockingQueue<DanmakuEntity> danmakuQueue = new LinkedBlockingQueue<>();

    // 控制处理线程的运行状态
    private final AtomicBoolean running = new AtomicBoolean(true);

    // 处理线程
    private Thread commentProcessThread;
    private Thread danmakuProcessThread;

    @Autowired
    public AiExaminePipe(AIConfigServer aiConfigServer,
                         CommentDao commentDao,
                         DanmakuDao danmakuDao,
                         NotificationService notificationService,
                         CountRecorder countRecorder) {
        this.aiConfigServer = aiConfigServer;
        this.commentDao = commentDao;
        this.danmakuDao = danmakuDao;
        this.notificationService = notificationService;
        this.countRecorder = countRecorder;
    }

    /**
     * 初始化处理线程
     */
    @PostConstruct
    public void init() {
        // 启动评论处理线程
        commentProcessThread = new Thread(this::processCommentQueue);
        commentProcessThread.setName("comment-examine-thread");
        commentProcessThread.start();

        // 启动弹幕处理线程
        danmakuProcessThread = new Thread(this::processDanmakuQueue);
        danmakuProcessThread.setName("danmaku-examine-thread");
        danmakuProcessThread.start();

        log.info("AI审核管道初始化完成");
    }

    /**
     * 关闭处理线程
     */
    @PreDestroy
    public void shutdown() {
        running.set(false);
        commentProcessThread.interrupt();
        danmakuProcessThread.interrupt();
        log.info("AI审核管道已关闭");
    }

    /**
     * 提交评论到审核队列
     *
     * @param comment 评论实体
     */
    @Async
    public void submitComment(CommentEntity comment) {
        try {
            commentQueue.put(comment);
            log.debug("评论已提交到审核队列, ID: {}", comment.getId());
        } catch (InterruptedException e) {
            log.error("提交评论到审核队列失败", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 提交弹幕到审核队列
     *
     * @param danmaku 弹幕实体
     */
    @Async
    public void submitDanmaku(DanmakuEntity danmaku) {
        try {
            danmakuQueue.put(danmaku);
            log.debug("弹幕已提交到审核队列, ID: {}", danmaku.getId());
        } catch (InterruptedException e) {
            log.error("提交弹幕到审核队列失败", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理评论队列
     */
    private void processCommentQueue() {
        while (running.get()) {
            try {
                // 从队列中取出评论
                CommentEntity comment = commentQueue.take();
                log.debug("开始处理评论, ID: {}", comment.getId());

                // 调用AI审核
                AiExamineCommentAndDanmakuResult result = aiConfigServer.aiExamineCommentAndDanmaku(comment.getComment());

                if (result != null) {
                    // 更新评论的审核结果
                    comment.setAiExamineMessage(result.getMessage());
                    comment.setAiExamineToken(result.getToken()); // 这里可以从AI结果中获取token数量
                    comment.setAiExamineId(Integer.parseInt(String.valueOf(result.getAiConfigId()))); // 使用的模型ID

                    // 如果审核不通过，删除内容
                    if (result.getResult()) {
                        comment.setStatus(TypeCode.NORMAL);
                    } else {
                        comment.setStatus(TypeCode.DELETE);
                        notificationService.sendNotification(
                                0,
                                comment.getUserId(),
                                comment.getArticleId(),
                                comment.getParentCommentId(),
                                comment.getId(),
                                "评论被删除",
                                "",
                                buildDeleteNotification("评论", comment.getComment(), result.getMessage(),
                                        comment.getId(), "comment"),
                                NotificationType.SYSTEM
                        );
                    }

                    // 更新评论
                    commentDao.updateById(comment);
                    countRecorder.recordAiModelToken(result.getAiConfigId(), result.getToken());
                    log.info("评论审核完成, ID: {}, 结果: {}", comment.getId(), result.getResult());
                } else {
                    log.warn("评论审核失败, AI返回结果为空,需要人工审核 ID: {}", comment.getId());
                }
            } catch (InterruptedException e) {
                log.warn("评论处理线程被中断", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("处理评论时发生错误", e);
            }
        }
    }

    /**
     * 处理弹幕队列
     */
    private void processDanmakuQueue() {
        while (running.get()) {
            try {
                // 从队列中取出弹幕
                DanmakuEntity danmaku = danmakuQueue.take();
                log.debug("开始处理弹幕, ID: {}", danmaku.getId());

                // 调用AI审核
                AiExamineCommentAndDanmakuResult result = aiConfigServer.aiExamineCommentAndDanmaku(danmaku.getText());

                if (result != null) {
                    // 更新弹幕的审核结果
                    danmaku.setAiExamineMessage(result.getMessage());
                    danmaku.setAiExamineToken(result.getToken()); // 这里可以从AI结果中获取token数量
                    danmaku.setAiExamineId(Integer.parseInt(String.valueOf(result.getAiConfigId()))); // 使用的模型ID

                    // 如果审核不通过，可以设置弹幕状态为不可见
                    if (result.getResult()) {
                        danmaku.setStatus(TypeCode.NORMAL);
                    } else {
                        danmaku.setStatus(TypeCode.DELETE);
                        notificationService.sendNotification(
                                0,
                                danmaku.getAuthor(),
                                danmaku.getVideoId(),
                                danmaku.getVideoId(),
                                danmaku.getId(),
                                "弹幕被删除",
                                "",
                                buildDeleteNotification("弹幕", danmaku.getText(), result.getMessage(),
                                        danmaku.getId(), "danmaku"),
                                NotificationType.SYSTEM
                        );
                    }

                    // 更新弹幕
                    danmakuDao.updateById(danmaku);
                    countRecorder.recordAiModelToken(result.getAiConfigId(), result.getToken());
                    log.info("弹幕审核完成, ID: {}, 结果: {}", danmaku.getId(), result.getResult());
                } else {
                    log.warn("评论审核失败, AI返回结果为空,需要人工审核 ID：: {}", danmaku.getId());
                }
            } catch (InterruptedException e) {
                log.warn("弹幕处理线程被中断", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("处理弹幕时发生错误", e);
            }
        }
    }

    /**
     * 内容被 AI 审核删除时发给用户的通知正文。
     * 用户原文与模型理由一律转成转义后的纯文本，只有申诉链接是系统自己生成的可信 HTML。
     */
    private String buildDeleteNotification(String target, String originalText, String aiMessage,
                                           Long id, String appealType) {
        return "你好，你的" + target + "：" + MyStringUtils.safeTextForRichMessage(originalText, 50)
                + " 因为「" + MyStringUtils.safeTextForRichMessage(aiMessage, 100) + "」已被删除，请遵相关法律法规及社区规定。"
                + "如有异议，请 【<a href=\"/appeal?id=" + id + "&type=" + appealType + "\">点击此处</a>】 进行申诉。";
    }

    /**
     * 获取评论队列大小
     */
    public int getCommentQueueSize() {
        return commentQueue.size();
    }

    /**
     * 获取弹幕队列大小
     */
    public int getDanmakuQueueSize() {
        return danmakuQueue.size();
    }
}
