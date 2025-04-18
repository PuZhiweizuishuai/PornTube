package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频，图片，文章 发帖表

TODO 回复消息可见，加密帖子，视频等
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface ArticleDao extends BaseMapper<ArticleEntity> {
    void addDanmakuCount(@Param("id") long articleId, @Param("count") Long count);


    void addViewCount(@Param("id") long articleId, @Param("count") Long count);

    void addCount(@Param("col") String col, @Param("id") long articleId, @Param("count") Long count);
    
    /**
     * 查询热门文章，直接在数据库层计算热度值
     * @param startTime 开始时间（24小时内）
     * @param limit 限制数量
     * @return 热门文章列表
     */
    List<ArticleEntity> findHotArticlesWithScore(@Param("startTime") long startTime, @Param("limit") int limit);
    
    /**
     * 查询最新发布的文章并计算热度值
     * @param limit 限制数量
     * @return 按创建时间排序的最新文章列表
     */
    List<ArticleEntity> findLatestArticlesWithScore(@Param("limit") int limit);
}
