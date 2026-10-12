package com.buguagaoshu.tiktube.schedule;

import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.cache.VideoCatalogCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定期计算热门内容
 *
 * 排行榜和标签索引都在启动后尽快算出第一版，进程刚起来的几分钟不再依赖慢查询兜底。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-12
 */
@Component
@Slf4j
public class HotTasks {

    /**
     * 排行榜刷新间隔，榜单本身只是把快照换成新的一份，半小时内没人看第二次也不浪费查询
     */
    private static final long HOT_REFRESH_MILLIS = 1_800_000L;

    /**
     * 标签倒排索引重建间隔，比榜单低频，一次要投影扫上万行
     */
    private static final long TAG_INDEX_REFRESH_MILLIS = 7_200_000L;

    private final ArticleService articleService;

    private final HotCache hotCache;

    private final VideoCatalogCache videoCatalogCache;

    @Autowired
    public HotTasks(ArticleService articleService, HotCache hotCache, VideoCatalogCache videoCatalogCache) {
        this.articleService = articleService;
        this.hotCache = hotCache;
        this.videoCatalogCache = videoCatalogCache;
    }

    @Scheduled(fixedDelay = HOT_REFRESH_MILLIS, initialDelay = 3000)
    public void hotTask() {
        log.info("开始计算当前热门内容......");
        hotCache.update(articleService.hotView(WebConstant.HOT_NUM));
        log.info("热门内容计算完成。");
    }

    @Scheduled(fixedDelay = TAG_INDEX_REFRESH_MILLIS, initialDelay = 1000)
    public void tagIndexTask() {
        log.info("开始重建视频标签索引......");
        videoCatalogCache.refresh();
    }
}
