package com.buguagaoshu.tiktube.vo;

import lombok.Data;

/**
 * 数据分析「分区分布」的一行
 *
 * 只返回二级分区 id，分区名由前端用 /api/category/tree 的映射表渲染，
 * 后端不重复维护一份分类缓存。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Data
public class CreatorCategoryStat {

    /** 二级分区 ID */
    private Integer category;

    /** 该分区下的稿件数 */
    private long articleCount;

    /** 区间观看人数 */
    private long periodView;

    /** 累计播放量 */
    private long viewCount;
}
