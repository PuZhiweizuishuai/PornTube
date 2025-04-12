package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.WebSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
@Mapper
public interface WebSettingDao extends BaseMapper<WebSettingEntity> {
	WebSettingEntity findNewSetting();
}
