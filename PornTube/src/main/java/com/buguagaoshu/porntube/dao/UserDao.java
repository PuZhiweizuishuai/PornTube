package com.buguagaoshu.porntube.dao;

import com.buguagaoshu.porntube.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
	void addSubmitCount(@Param("userId") long userId, @Param("count") int count);
}
