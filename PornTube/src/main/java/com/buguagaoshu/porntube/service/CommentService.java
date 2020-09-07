package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.CommentEntity;

import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface CommentService extends IService<CommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

