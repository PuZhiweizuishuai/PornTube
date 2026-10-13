package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.LikeTableEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 点赞
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface LikeTableDao extends BaseMapper<LikeTableEntity> {

    /**
     * 批量查询某用户在一批对象里的点赞状态，返回其中已点赞的对象 ID，
     * 用于列表接口一次带出点赞状态，替代前端逐条查询
     *
     * @param type 【0 视频图片文章主帖子  1 评论】
     */
    List<Long> selectLikedObjIds(@Param("objIds") Collection<Long> objIds,
                                 @Param("type") Integer type,
                                 @Param("userId") Long userId);
}
