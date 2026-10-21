package com.buguagaoshu.tiktube.pipe;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.WebConfigCache;
import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.dao.CommentDao;
import com.buguagaoshu.tiktube.dao.DanmakuDao;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.enums.TypeCode;
import com.buguagaoshu.tiktube.model.AiExamineCommentAndDanmakuResult;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import com.buguagaoshu.tiktube.service.AIConfigServer;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.utils.MyStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-17
 * AI 审核管道
 * <p>
 * 队列只是「尽快审核」的快路径：评论/弹幕入库时已经是待审状态，进程重启或队列满丢掉的条目
 * 由 {@link #recoverStuckExamine()} 定时从库里捞回来，所以队列有界也不会丢审核。
 * 多实例部署时恢复扫描靠 Redis 上的 NX 锁保证同一批待审只被一个实例捞走。
 */
@Slf4j
@Component
public class AiExaminePipe {

    /**
     * 队列容量上限。AI 变慢时最多积压这么多，超出的直接留在库里等恢复扫描，不让内存无限涨
     */
    private static final int QUEUE_CAPACITY = 1000;

    /**
     * 恢复扫描间隔，毫秒
     */
    private static final long RECOVERY_INTERVAL_MILLIS = 300_000L;

    /**
     * 只捞提交超过这个时长仍处于待审且从未审核过的条目，避开刚入库还在队列里的正常条目
     */
    private static final long RECOVERY_MIN_AGE_MILLIS = 60_000L;

    private static final int RECOVERY_BATCH = 200;

    private static final String RECOVERY_LOCK_KEY = "ai:examine:recovery:lock";

    /**
     * 锁的有效期要短于扫描间隔，持有实例宕机后下一轮别的实例才能接管
     */
    private static final long RECOVERY_LOCK_SECONDS = 240L;

    private static final String AI_FAILED_NOTE = "AI 审核未返回结果，等待人工审核";

    private final AIConfigServer aiConfigServer;
    private final CommentDao commentDao;
    private final DanmakuDao danmakuDao;
    private final NotificationService notificationService;
    private final WebConfigCache webConfigCache;
    private final MyConfigProperties configProperties;
    private final RedisRepository redisRepository;

    // 记录 TOKEN 消耗
    private final CountRecorder countRecorder;

    private final String instanceId = UUID.randomUUID().toString();

    // 评论审核队列
    private final BlockingQueue<CommentEntity> commentQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    // 弹幕审核队列
    private final BlockingQueue<DanmakuEntity> danmakuQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

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
                         WebConfigCache webConfigCache,
                         MyConfigProperties configProperties,
                         RedisRepository redisRepository,
                         CountRecorder countRecorder) {
        this.aiConfigServer = aiConfigServer;
        this.commentDao = commentDao;
        this.danmakuDao = danmakuDao;
        this.notificationService = notificationService;
        this.webConfigCache = webConfigCache;
        this.configProperties = configProperties;
        this.redisRepository = redisRepository;
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
    public void submitComment(CommentEntity comment) {
        // offer 不阻塞：队列满时这条评论已经是库里的待审状态，恢复扫描会把它捞回来
        if (!commentQueue.offer(comment)) {
            log.warn("评论审核队列已满（{}），评论 {} 留待恢复扫描或人工审核", QUEUE_CAPACITY, comment.getId());
        }
    }

    /**
     * 提交弹幕到审核队列
     *
     * @param danmaku 弹幕实体
     */
    public void submitDanmaku(DanmakuEntity danmaku) {
        if (!danmakuQueue.offer(danmaku)) {
            log.warn("弹幕审核队列已满（{}），弹幕 {} 留待恢复扫描或人工审核", QUEUE_CAPACITY, danmaku.getId());
        }
    }

    /**
     * 把卡在待审、且从未被 AI 审核过的评论/弹幕捞回队列。
     * 覆盖进程重启丢队列、队列满被丢弃这两类情况；AI 审核失败会留下说明文字，因此不会被反复重试
     */
    @Scheduled(fixedDelay = RECOVERY_INTERVAL_MILLIS, initialDelay = RECOVERY_INTERVAL_MILLIS)
    public void recoverStuckExamine() {
        if (!Boolean.TRUE.equals(webConfigCache.getWebConfigData().getOpenAIConfig())) {
            return;
        }
        if (!tryAcquireRecoveryLock()) {
            return;
        }
        long cutoff = System.currentTimeMillis() - RECOVERY_MIN_AGE_MILLIS;
        try {
            List<CommentEntity> comments = commentDao.selectList(new QueryWrapper<CommentEntity>()
                    .eq("status", TypeCode.EXAM)
                    .isNull("ai_examine_message")
                    .lt("create_time", cutoff)
                    .orderByAsc("id")
                    .last("LIMIT " + RECOVERY_BATCH));
            for (CommentEntity comment : comments) {
                if (!commentQueue.offer(comment)) {
                    break;
                }
            }
            List<DanmakuEntity> danmakus = danmakuDao.selectList(new QueryWrapper<DanmakuEntity>()
                    .eq("status", TypeCode.EXAM)
                    .isNull("ai_examine_message")
                    .lt("create_time", cutoff)
                    .orderByAsc("id")
                    .last("LIMIT " + RECOVERY_BATCH));
            for (DanmakuEntity danmaku : danmakus) {
                if (!danmakuQueue.offer(danmaku)) {
                    break;
                }
            }
            if (!comments.isEmpty() || !danmakus.isEmpty()) {
                log.info("恢复扫描捞回待审评论 {} 条、弹幕 {} 条", comments.size(), danmakus.size());
            }
        } catch (Exception e) {
            log.warn("恢复扫描待审内容失败，下一轮重试：{}", e.toString());
        }
    }

    /**
     * 多实例下同一批待审只能被一个实例捞走，否则会出现重复审核与重复的删除通知。
     * 未开启 Redis 时必然是单实例，不需要锁
     */
    private boolean tryAcquireRecoveryLock() {
        if (!(configProperties.getOpenRedis() && redisRepository.isReachable())) {
            return true;
        }
        return redisRepository.setIfAbsent(RECOVERY_LOCK_KEY, instanceId, RECOVERY_LOCK_SECONDS);
    }

    /**
     * 处理评论队列
     */
    private void processCommentQueue() {
        while (running.get()) {
            try {
                // 从队列中取出评论
                CommentEntity queued = commentQueue.take();
                log.debug("开始处理评论, ID: {}", queued.getId());

                // 队列里的是入队时的旧副本：恢复扫描与提交快路径可能把同一条放进队列两次，
                // 也可能排队期间已被人工处理，处理前一律以库里的现状为准
                CommentEntity comment = commentDao.selectById(queued.getId());
                if (comment == null || comment.getStatus() == null
                        || comment.getStatus() != TypeCode.EXAM || comment.getAiExamineMessage() != null) {
                    continue;
                }

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
                    // 留下说明文字：恢复扫描以 ai_examine_message 为空作为「从未审核过」的判据，
                    // 不写的话这条会被每轮扫描反复捞回来重复请求模型
                    comment.setAiExamineMessage(AI_FAILED_NOTE);
                    commentDao.updateById(comment);
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
                DanmakuEntity queued = danmakuQueue.take();
                log.debug("开始处理弹幕, ID: {}", queued.getId());

                DanmakuEntity danmaku = danmakuDao.selectById(queued.getId());
                if (danmaku == null || danmaku.getStatus() == null
                        || danmaku.getStatus() != TypeCode.EXAM || danmaku.getAiExamineMessage() != null) {
                    continue;
                }

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
                    danmaku.setAiExamineMessage(AI_FAILED_NOTE);
                    danmakuDao.updateById(danmaku);
                    log.warn("弹幕审核失败, AI返回结果为空,需要人工审核 ID: {}", danmaku.getId());
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
