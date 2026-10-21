package com.buguagaoshu.tiktube.schedule;

import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.cache.SharedCache;
import com.buguagaoshu.tiktube.cache.SharedCacheVersionStore;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.repository.impl.FileRepositoryInOSS;
import com.buguagaoshu.tiktube.service.AIConfigServer;
import com.buguagaoshu.tiktube.service.AdvertisementService;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.CategoryService;
import com.buguagaoshu.tiktube.service.WebConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-19
 * 开启 open-redis 后，把各实例内存里的快照缓存对齐到同一版。
 * <p>
 * 每个实例自己那份快照的读路径始终在内存里，这里只负责发现「别的实例已经回源刷新过」，
 * 然后按 {@link SharedCache} 逐个重建自己的快照，所以管理员在任意一个实例上改设置，
 * 其他实例最多滞后一个轮询间隔就能生效。
 */
@Slf4j
@Component
public class SharedCacheSyncTasks {

    private final SharedCacheVersionStore versionStore;

    private final Map<SharedCache, BooleanSupplier> reloaders = new EnumMap<>(SharedCache.class);

    @Autowired
    public SharedCacheSyncTasks(SharedCacheVersionStore versionStore,
                                ArticleService articleService,
                                HotCache hotCache,
                                CategoryService categoryService,
                                WebConfigService webConfigService,
                                AIConfigServer aiConfigServer,
                                AdvertisementService advertisementService,
                                FileRepositoryInOSS fileRepositoryInOSS) {
        this.versionStore = versionStore;
        // 重建方式与各缓存首次加载的方式保持一致，回源取数即可，不需要从 Redis 搬运内容
        reloaders.put(SharedCache.HOT_RANKING, () -> {
            hotCache.update(articleService.hotView(WebConstant.HOT_NUM), true);
            return true;
        });
        reloaders.put(SharedCache.CATEGORY, () -> {
            categoryService.refreshCache();
            return true;
        });
        reloaders.put(SharedCache.WEB_CONFIG, () -> {
            webConfigService.refreshCache();
            return true;
        });
        reloaders.put(SharedCache.AI_CONFIG, () -> {
            aiConfigServer.setCacheConfig();
            return true;
        });
        reloaders.put(SharedCache.ADVERTISEMENT, () -> {
            advertisementService.getNowAdvertisement(true);
            return true;
        });
        reloaders.put(SharedCache.OSS_CONFIG, () -> {
            fileRepositoryInOSS.updateS3Client();
            return true;
        });
    }

    /**
     * 本地版本与 Redis 版本不一致就重建，包括进程启动后的第一轮：
     * 启动加载与首轮轮询之间别的实例可能已经改过数据，只「对齐版本号」会把这段窗口里的更新吞掉。
     */
    @Scheduled(fixedDelayString = "${tiktube.cache-sync-interval-millis:15000}", initialDelay = 30_000)
    public void syncCacheVersions() {
        if (!versionStore.isShared()) {
            return;
        }
        try {
            for (Map.Entry<SharedCache, BooleanSupplier> entry : reloaders.entrySet()) {
                SharedCache cache = entry.getKey();
                Long remote = versionStore.remoteVersion(cache);
                if (remote == null) {
                    continue;
                }
                Long applied = versionStore.appliedVersion(cache);
                if (remote.equals(applied)) {
                    continue;
                }
                log.info("缓存 {} 版本不一致（Redis {}，本地 {}），开始重建本地快照", cache, remote, applied);
                versionStore.rebuild(cache, entry.getValue(), remote);
            }
        } catch (Exception e) {
            // Redis 抖动时整轮跳过，下个周期再试；不要每个缓存各打一遍堆栈
            log.warn("读取 Redis 缓存版本失败，本轮同步跳过：{}", e.toString());
        }
    }
}
