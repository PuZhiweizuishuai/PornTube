package com.buguagaoshu.porntube.dao;

import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
