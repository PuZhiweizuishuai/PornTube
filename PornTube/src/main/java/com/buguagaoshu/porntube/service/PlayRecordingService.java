package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.PlayRecordingEntity;

import java.util.Map;

/**
 * 播放记录
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface PlayRecordingService extends IService<PlayRecordingEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

