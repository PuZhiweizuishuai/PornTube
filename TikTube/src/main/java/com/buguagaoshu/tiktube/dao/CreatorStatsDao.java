package com.buguagaoshu.tiktube.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.buguagaoshu.tiktube.model.CreatorTotals;
import com.buguagaoshu.tiktube.model.DailyCountRow;
import com.buguagaoshu.tiktube.vo.CreatorArticleStat;
import com.buguagaoshu.tiktube.vo.CreatorCategoryStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 创作中心数据统计
 *
 * 只做「按天聚合」与「累计聚合」两件事，全部 SQL 见 CreatorStatsDao.xml。
 * 所有查询都以 user_id 为强制条件，调用方必须传入当前登录用户 ID，
 * 不存在「查别人的数据」的入口。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Mapper
public interface CreatorStatsDao {

    /**
     * 稿件的累计统计与审核状态分布
     *
     * @param userId 创作者 ID
     * @param status 稿件状态，传 {@code ArticleStatusEnum.NORMAL.getCode()}
     */
    CreatorTotals selectTotals(@Param("userId") long userId, @Param("status") int status);

    /**
     * 按天聚合的六项互动新增
     *
     * @param userId    创作者 ID
     * @param startTime 区间起点（含），应为当地 00:00
     * @param endTime   区间终点（不含）
     * @param tzOffset  本地时区相对 UTC 的偏移毫秒，用于把时间戳对齐到自然日
     * @param dayMillis 一天的毫秒数
     */
    List<DailyCountRow> selectDailyCounts(@Param("userId") long userId,
                                          @Param("startTime") long startTime,
                                          @Param("endTime") long endTime,
                                          @Param("tzOffset") long tzOffset,
                                          @Param("dayMillis") long dayMillis,
                                          @Param("status") int status);

    /**
     * 稿件表现榜（分页）
     *
     * @param orderSql 只能取服务层白名单里的常量，禁止拼接用户输入
     */
    IPage<CreatorArticleStat> selectArticleStats(IPage<?> page,
                                                 @Param("userId") long userId,
                                                 @Param("startTime") long startTime,
                                                 @Param("endTime") long endTime,
                                                 @Param("status") int status,
                                                 @Param("orderSql") String orderSql);

    /**
     * 分区分布
     */
    List<CreatorCategoryStat> selectCategoryStats(@Param("userId") long userId,
                                                  @Param("startTime") long startTime,
                                                  @Param("endTime") long endTime,
                                                  @Param("status") int status);
}
