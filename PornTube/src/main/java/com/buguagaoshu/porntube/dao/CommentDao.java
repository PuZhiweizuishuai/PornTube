package com.buguagaoshu.porntube.dao;

import com.buguagaoshu.porntube.entity.CommentEntity;
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
public interface CommentDao extends BaseMapper<CommentEntity> {
    void addCount(@Param("col") String col, @Param("id") long commentId, @Param("count") Long count);
}
