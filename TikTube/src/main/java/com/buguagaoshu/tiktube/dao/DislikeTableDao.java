package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.DislikeTableEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点踩
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface DislikeTableDao extends BaseMapper<DislikeTableEntity> {
	
}
