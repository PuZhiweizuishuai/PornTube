package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 弹幕表
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-07 16:00:19
 */
@Mapper
public interface DanmakuDao extends BaseMapper<DanmakuEntity> {
}
