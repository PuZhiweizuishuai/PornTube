package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
}

