package com.buguagaoshu.tiktube.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-23
 */
@ConfigurationProperties(
        prefix = "tiktube"
)
@Data
public class MyConfigProperties {
    private String ipDbPath;

    private Boolean isTheProxyConfigured = false;

    private Boolean openRedis = false;

    // 计数缓存（点赞/播放/弹幕等）批量落库间隔，毫秒
    private long countSyncIntervalMillis = 300_000;

    // 首次落库延迟，毫秒
    private long countSyncInitialDelayMillis = 30_000;

    // 单条批量 UPDATE SQL 最多携带的计数条目数
    private int countSyncBatchSize = 500;

    // 同一 IP 对同一视频/广告的去重窗口，毫秒，与落库间隔解耦
    private long playDedupWindowMillis = 21_600_000;

    // 播放记录缓冲批量落库间隔，毫秒。观看请求只写内存，历史记录最多滞后一个间隔
    private long playRecordFlushIntervalMillis = 60_000;

    // 单条批量 upsert SQL 最多携带的播放记录数（UA 较长，比计数批量取更小的默认值）
    private int playRecordBatchSize = 200;
}
