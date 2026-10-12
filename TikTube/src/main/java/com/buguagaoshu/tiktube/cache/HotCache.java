package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.vo.ArticleViewData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 热门内容缓存
 *
 * 定时任务算好一份整榜，接口只读这份快照。
 * 用不可变列表整体替换，避免原来直接暴露 {@code static List} 时
 * 写入方 clear/重新填充、读取方正在遍历导致的并发问题。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-12
 */
@Component
public class HotCache {

    private volatile List<ArticleViewData> hotList = List.of();

    /**
     * 最近一次榜单算好的时间，用于判断排行榜是否还新鲜
     */
    private volatile long buildTime;

    public void update(List<ArticleViewData> newHotList) {
        hotList = List.copyOf(newHotList);
        buildTime = System.currentTimeMillis();
    }

    /**
     * 当前热门榜快照，调用方不要再往里写
     */
    public List<ArticleViewData> getHotList() {
        return hotList;
    }

    public long getBuildTime() {
        return buildTime;
    }
}
