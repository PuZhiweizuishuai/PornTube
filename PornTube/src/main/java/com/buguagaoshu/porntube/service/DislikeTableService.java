package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.DislikeTableEntity;

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
}

