package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.NotificationEntity;

import java.util.Map;

/**
 * 通知表
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface NotificationService extends IService<NotificationEntity> {

    PageUtils queryPage(Map<String, Object> params);


    /**
     * 发送通知
     * @param notifier 发送人
     * @param receiver 接收人
     * @param outerId 帖子ID
     * @param commentId 评论ID
     * @param linkMessage 链接消息
     * @param content 消息内容
     * @param type 消息类型
     * */
    void sendNotification(
            long notifier,
            long receiver,
            long outerId,
            long articleId,
            long commentId,
            String title,
            String linkMessage,
            String content,
            NotificationType type
    );


    /**
     * 查询通知
     * */
    PageUtils queryNotification(Map<String, Object> params, Long userId);


    boolean readNotification(NotificationEntity notification, Long userId);


    boolean readAllNotification(Long userId);

    long countNotification(Long userId);
}

