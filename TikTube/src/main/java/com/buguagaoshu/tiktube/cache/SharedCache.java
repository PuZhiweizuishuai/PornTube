package com.buguagaoshu.tiktube.cache;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-19
 * 需要在多实例之间对齐的快照缓存。
 * <p>
 * 这些缓存的内容都是从库里算出来的一份快照，读得极勤（拦截器、列表页、播放页每次都要读），
 * 所以 Redis 里只放「这份快照是哪一版」的版本号，各实例读到新版本后各自回源重建自己内存里的那份，
 * 读路径不因为开了 Redis 而多出网络往返，复杂的实体也不必序列化进 Redis。
 */
public enum SharedCache {

    /**
     * 热门榜，由 {@code HotTasks} 定时算好
     */
    HOT_RANKING("cache:version:hot"),

    /**
     * 分区分类树
     */
    CATEGORY("cache:version:category"),

    /**
     * 站点设置
     */
    WEB_CONFIG("cache:version:webConfig"),

    /**
     * AI 配置与各自的 token 余量
     */
    AI_CONFIG("cache:version:aiConfig"),

    /**
     * 当前生效的广告
     */
    ADVERTISEMENT("cache:version:ads"),

    /**
     * 对象存储配置与各配置的 S3 客户端。
     * 后台改配置时只有接到请求的那个实例会重建客户端，不同步的话其他实例会继续往旧桶里写文件，
     * 并把错误的落盘位置写进数据库
     */
    OSS_CONFIG("cache:version:oss");

    private final String versionKey;

    SharedCache(String versionKey) {
        this.versionKey = versionKey;
    }

    /**
     * Redis 里存版本号的键，值是写入方刷新完快照时自增的版本序号
     */
    public String versionKey() {
        return versionKey;
    }
}
