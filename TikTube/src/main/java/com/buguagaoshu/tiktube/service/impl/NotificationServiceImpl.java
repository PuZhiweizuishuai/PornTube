package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.enums.NotificationType;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.vo.NotificationAndSendUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;
import com.buguagaoshu.tiktube.dao.NotificationDao;
import com.buguagaoshu.tiktube.entity.NotificationEntity;
import com.buguagaoshu.tiktube.service.NotificationService;


@Service("notificationService")
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, NotificationEntity> implements NotificationService {

    final UserService userService;

    @Autowired
    public NotificationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<NotificationEntity> page = this.page(
                new Query<NotificationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }


    @Override
    public void sendNotification(
            long notifier,
            long receiver,
            long outerId,
            long articleId,
            long commentId,
            String title,
            String linkMessage,
            String content,
            NotificationType type
    ) {
        long time = System.currentTimeMillis();
        NotificationEntity notification = new NotificationEntity();
        notification.setArticleId(articleId);
        notification.setTitle(title);
        notification.setNotifier(notifier);
        notification.setReceiver(receiver);
        notification.setOuterId(outerId);
        notification.setLinkMessage(linkMessage);
        notification.setContent(content);
        notification.setType(type.getType());
        notification.setCommentId(commentId);
        notification.setStatus(NotificationType.UNREAD.getType());
        notification.setCreateTime(time);
        // 发送与接收不是同一个人才通知
        if (notifier != receiver) {
            this.save(notification);
        }
    }

    @Override
    public PageUtils queryNotification(Map<String, Object> params, Long userId) {
        QueryWrapper<NotificationEntity> wrapper = new QueryWrapper<>();
        String type = (String) params.get("type");
        String status = (String) params.get("status");
        wrapper.eq("receiver", userId);
        if (type != null) {
            wrapper.eq("type", Integer.parseInt(type));
        }
        if (status != null) {
            wrapper.eq("status", Integer.parseInt(status));
        }
        wrapper.orderByDesc("create_time");
        IPage<NotificationEntity> page = this.page(
                new Query<NotificationEntity>().getPage(params),
                wrapper
        );
        if (page.getRecords().isEmpty()) {
            return new PageUtils(page);
        }
        List<NotificationEntity> notifications = page.getRecords();
        Set<Long> userIds = notifications.stream().map(NotificationEntity::getNotifier).collect(Collectors.toSet());
        Map<Long, UserEntity> longUserEntityMap = userService.userMapList(userIds);
        List<NotificationAndSendUser> notificationAndSendUsers = new ArrayList<>();

        for (NotificationEntity notification : notifications) {
            NotificationAndSendUser notificationAndSendUser = new NotificationAndSendUser();
            UserEntity userEntity = longUserEntityMap.get(notification.getNotifier());
            userEntity.setMail("");
            userEntity.setPassword("");
            userEntity.setPhone("");
            notificationAndSendUser.setUser(userEntity);
            notificationAndSendUser.setNotification(notification);
            notificationAndSendUsers.add(notificationAndSendUser);
        }

        return new PageUtils(notificationAndSendUsers, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public boolean readNotification(NotificationEntity notification, Long userId) {
        NotificationEntity byId = this.getById(notification.getId());
        if (byId != null) {
            if (Objects.equals(byId.getReceiver(), userId)) {
                byId.setReadTime(System.currentTimeMillis());
                byId.setStatus(NotificationType.READ.getType());
                this.updateById(byId);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean readAllNotification(Long userId) {
        if (userId == null) {
            return false;
        }
        // 获取当前时间作为阅读时间
        long readTime = System.currentTimeMillis();
        // 调用DAO层的方法批量更新通知状态
        int rows = this.baseMapper.updateAllNotificationStatus(userId, NotificationType.READ.getType(), readTime);
        // 如果更新了记录，则返回成功
        return rows > 0;
    }

    @Override
    public long countNotification(Long userId) {
        QueryWrapper<NotificationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver", userId);
        wrapper.eq("status", NotificationType.UNREAD.getType());
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 10");
        return this.list(wrapper).size();
    }
}