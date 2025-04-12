package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.LikeTableEntity;

import java.util.Map;

/**
 * 点赞
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface LikeTableService extends IService<LikeTableEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * type: 【0 视频图片文章主帖子  1 评论】
     * */
    Map<String, Object> toggleLike(Long likeObjId, Integer type, Long userId);


    LikeTableEntity checkLike(Long likeObjId, Integer type, Long userId);
}

