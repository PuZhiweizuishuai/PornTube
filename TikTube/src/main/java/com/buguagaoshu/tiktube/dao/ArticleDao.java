package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
     * 批量更新文章计数
     * @param col 要更新的列名
     * @param countMap ID和计数的映射，key是文章ID，value是要增加的数值
     * @return 更新的记录数
     */
    int batchUpdateCount(@Param("col") String col, @Param("countMap") Map<Long, Long> countMap);
    
    /**
     * 召回热门候选，热度分由数据库按时间衰减公式算好并排序
     *
     * @param now         查询时刻的毫秒时间戳，衰减以它为零点
     * @param windowStart 只取该时间之后发布的稿件，传 null 表示不加时间窗
     * @param limit       候选数量
     * @param type        稿件类型
     */
    List<ArticleEntity> findTrendingVideos(@Param("now") long now,
                                           @Param("windowStart") Long windowStart,
                                           @Param("limit") int limit,
                                           @Param("type") int type);

    /**
     * 标签倒排索引的投影查询，只取打分需要的列
     *
     * @param type  稿件类型
     * @param limit 最多加载多少篇
     */
    List<ArticleEntity> findVideosForTagIndex(@Param("type") int type, @Param("limit") int limit);

    /**
     * 根据标签查找相似文章，只在倒排索引不可用时作为回退路径
     *
     * @param tagLikeList 标签匹配模式，服务层按 JSON 数组元素精确构造
     * @param articleId   当前文章ID（排除自身）
     * @param limit       限制数量
     * @param type        稿件类型
     */
    List<ArticleEntity> findSimilarArticlesByTags(@Param("tagLikeList") List<String> tagLikeList,
                                                  @Param("articleId") Long articleId,
                                                  @Param("limit") int limit,
                                                  @Param("type") int type);
}
