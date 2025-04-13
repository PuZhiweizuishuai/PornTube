package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.DislikeTableEntity;

import java.util.Map;

/**
 * 点踩
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface DislikeTableService extends IService<DislikeTableEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * type: 【0 帖子视频图片， 1 评论】
     */
    Map<String, Object> toggleDislike(Long dislikeObjId, Integer type, Long userId);

    DislikeTableEntity checkDislike(Long dislikeObjId, Integer type, Long userId);
}

