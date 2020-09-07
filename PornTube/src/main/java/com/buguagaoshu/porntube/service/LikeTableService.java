package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.LikeTableEntity;

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
}

