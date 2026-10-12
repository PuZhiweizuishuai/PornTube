package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-22
 * 缓存计数器
 * <p>
 * 计数先写入内存/Redis 缓冲，由定时任务批量落库。
 * 落库方通过 {@link #drain(CountFamily)} 原子取走当前缓冲（取走后新计数写入全新缓冲，
 * 不会丢失窗口），落库失败再 {@link #restore(CountFamily, Map)} 并回，下一轮重试。
 */
@Component
public class CountRecorder {

    /**
     * 计数族，枚举值与落库目标一一对应；Redis 模式下每个族是一个 Hash（field 为对象 ID）
     */
    public enum CountFamily {
        ARTICLE_COMMENT("count:article:comment"),
        ARTICLE_LIKE("count:article:like"),
        ARTICLE_FAVORITE("count:article:favorite"),
        ARTICLE_DISLIKE("count:article:dislike"),
        COMMENT_COUNT("count:comment:count"),
        COMMENT_LIKE("count:comment:like"),
        COMMENT_DISLIKE("count:comment:dislike"),
        DANMAKU("count:danmaku"),
        AI_TOKEN("count:ai:token");

        private final String redisHashKey;

        CountFamily(String redisHashKey) {
            this.redisHashKey = redisHashKey;
        }

        public String redisHashKey() {
            return redisHashKey;
        }
    }

    private final CountStorage countStorage;

    @Autowired
    public CountRecorder(RedisRepository redisRepository, MyConfigProperties configProperties) {
        // 根据配置决定使用哪种存储实现
        if (configProperties.getOpenRedis() && redisRepository.isAvailable()) {
            this.countStorage = new RedisCountStorage(redisRepository);
        } else {
            this.countStorage = new MemoryCountStorage();
        }
    }

    /**
     * 稿件数据同步
     * */
    public void syncArticleCount(ArticleEntity articleEntity) {
        articleEntity.setCommentCount(articleEntity.getCommentCount() + getArticleCommentCount(articleEntity.getId()));
        articleEntity.setLikeCount(articleEntity.getLikeCount() + getArticleLikeCount(articleEntity.getId()));
        articleEntity.setFavoriteCount(articleEntity.getFavoriteCount() + getArticleFavoriteCount(articleEntity.getId()));
        articleEntity.setDislikeCount(articleEntity.getDislikeCount() + getArticleDislikeCount(articleEntity.getId()));
        articleEntity.setDanmakuCount(articleEntity.getDanmakuCount() + getDanmakuCount(articleEntity.getId()));
    }

    public void syncCommentCount(CommentEntity comment) {
        comment.setCommentCount(comment.getCommentCount() + getCommentCount(comment.getId()));
        comment.setLikeCount(comment.getLikeCount() + getCommentLikeCount(comment.getId()));
        comment.setDislikeCount(comment.getDislikeCount() + getCommentDislikeCount(comment.getId()));
    }

    /**
     * 原子取走某个计数族的当前缓冲，返回的 Map 与后续写入完全隔离
     */
    public Map<Long, Long> drain(CountFamily family) {
        return countStorage.drain(family);
    }

    /**
     * 落库失败时把快照并回当前缓冲，等待下一轮重试
     */
    public void restore(CountFamily family, Map<Long, Long> snapshot) {
        countStorage.restore(family, snapshot);
    }

    // 文章评论计数相关方法
    public void recordArticleComment(long articleId, long count) {
        countStorage.record(CountFamily.ARTICLE_COMMENT, articleId, count);
    }

    public long getArticleCommentCount(long articleId) {
        return countStorage.get(CountFamily.ARTICLE_COMMENT, articleId);
    }

    public Map<Long, Long> getArticleCommentCountMap() {
        return countStorage.getSnapshot(CountFamily.ARTICLE_COMMENT);
    }

    // 评论计数相关方法
    public void recordComment(long commentId, Long count) {
        countStorage.record(CountFamily.COMMENT_COUNT, commentId, count);
    }

    public long getCommentCount(long commentId) {
        return countStorage.get(CountFamily.COMMENT_COUNT, commentId);
    }

    public Map<Long, Long> getCommentCountMap() {
        return countStorage.getSnapshot(CountFamily.COMMENT_COUNT);
    }

    // 弹幕计数相关方法
    public void recordDanmaku(long articleId, Long count) {
        countStorage.record(CountFamily.DANMAKU, articleId, count);
    }

    public long getDanmakuCount(long articleId) {
        return countStorage.get(CountFamily.DANMAKU, articleId);
    }

    public Map<Long, Long> getDanmakuCountMap() {
        return countStorage.getSnapshot(CountFamily.DANMAKU);
    }

    // 文章点赞计数相关方法
    public void recordArticleLike(long articleId, long count) {
        countStorage.record(CountFamily.ARTICLE_LIKE, articleId, count);
    }

    public long getArticleLikeCount(long articleId) {
        return countStorage.get(CountFamily.ARTICLE_LIKE, articleId);
    }

    public Map<Long, Long> getArticleLikeCountMap() {
        return countStorage.getSnapshot(CountFamily.ARTICLE_LIKE);
    }

    // 文章收藏计数相关方法
    public void recordArticleFavorite(long articleId, long count) {
        countStorage.record(CountFamily.ARTICLE_FAVORITE, articleId, count);
    }

    public long getArticleFavoriteCount(long articleId) {
        return countStorage.get(CountFamily.ARTICLE_FAVORITE, articleId);
    }

    public Map<Long, Long> getArticleFavoriteCountMap() {
        return countStorage.getSnapshot(CountFamily.ARTICLE_FAVORITE);
    }

    // 文章点踩计数相关方法
    public void recordArticleDislike(long articleId, long count) {
        countStorage.record(CountFamily.ARTICLE_DISLIKE, articleId, count);
    }

    public long getArticleDislikeCount(long articleId) {
        return countStorage.get(CountFamily.ARTICLE_DISLIKE, articleId);
    }

    public Map<Long, Long> getArticleDislikeCountMap() {
        return countStorage.getSnapshot(CountFamily.ARTICLE_DISLIKE);
    }

    // 评论点赞计数相关方法
    public void recordCommentLike(long commentId, long count) {
        countStorage.record(CountFamily.COMMENT_LIKE, commentId, count);
    }

    public long getCommentLikeCount(long commentId) {
        return countStorage.get(CountFamily.COMMENT_LIKE, commentId);
    }

    public Map<Long, Long> getCommentLikeCountMap() {
        return countStorage.getSnapshot(CountFamily.COMMENT_LIKE);
    }

    // 评论点踩计数相关方法
    public void recordCommentDislike(long commentId, long count) {
        countStorage.record(CountFamily.COMMENT_DISLIKE, commentId, count);
    }

    public long getCommentDislikeCount(long commentId) {
        return countStorage.get(CountFamily.COMMENT_DISLIKE, commentId);
    }

    public Map<Long, Long> getCommentDislikeCountMap() {
        return countStorage.getSnapshot(CountFamily.COMMENT_DISLIKE);
    }

    public void recordAiModelToken(Long modelId, long count) {
        countStorage.record(CountFamily.AI_TOKEN, modelId, count);
    }

    public long getAiModelTokenCount(Long modelId) {
        return countStorage.get(CountFamily.AI_TOKEN, modelId);
    }

    public Map<Long, Long> getAiModelTokenCountMap() {
        return countStorage.getSnapshot(CountFamily.AI_TOKEN);
    }

    // 计数存储接口
    private interface CountStorage {
        void record(CountFamily family, long id, long delta);

        long get(CountFamily family, long id);

        Map<Long, Long> getSnapshot(CountFamily family);

        Map<Long, Long> drain(CountFamily family);

        void restore(CountFamily family, Map<Long, Long> snapshot);
    }

    // 内存存储实现
    private static class MemoryCountStorage implements CountStorage {
        // ConcurrentHashMap 保证 drain 换表与 record 写入之间的可见性与原子性
        private final ConcurrentMap<CountFamily, ConcurrentMap<Long, Long>> buffers = new ConcurrentHashMap<>();

        MemoryCountStorage() {
            for (CountFamily family : CountFamily.values()) {
                buffers.put(family, new ConcurrentHashMap<>());
            }
        }

        @Override
        public void record(CountFamily family, long id, long delta) {
            buffers.get(family).compute(id, (k, v) -> v == null ? delta : v + delta);
        }

        @Override
        public long get(CountFamily family, long id) {
            return buffers.get(family).getOrDefault(id, 0L);
        }

        @Override
        public Map<Long, Long> getSnapshot(CountFamily family) {
            return buffers.get(family);
        }

        @Override
        public Map<Long, Long> drain(CountFamily family) {
            return buffers.replace(family, new ConcurrentHashMap<>());
        }

        @Override
        public void restore(CountFamily family, Map<Long, Long> snapshot) {
            ConcurrentMap<Long, Long> current = buffers.get(family);
            snapshot.forEach((id, delta) -> current.compute(id, (k, v) -> v == null ? delta : v + delta));
        }
    }

    // Redis存储实现：每个计数族一个 Hash，读写均为单命令，不再 SCAN/逐键往返
    private static class RedisCountStorage implements CountStorage {
        private final RedisRepository redisRepository;

        public RedisCountStorage(RedisRepository redisRepository) {
            this.redisRepository = redisRepository;
        }

        @Override
        public void record(CountFamily family, long id, long delta) {
            redisRepository.hIncrBy(family.redisHashKey(), Long.toString(id), delta);
        }

        @Override
        public long get(CountFamily family, long id) {
            return redisRepository.hGetCount(family.redisHashKey(), Long.toString(id));
        }

        @Override
        public Map<Long, Long> getSnapshot(CountFamily family) {
            return toIdMap(redisRepository.hGetAllCounts(family.redisHashKey()));
        }

        @Override
        public Map<Long, Long> drain(CountFamily family) {
            // Lua HGETALL+DEL 原子排空，多实例并发刷库也不会重复计数
            return toIdMap(redisRepository.hDrain(family.redisHashKey()));
        }

        @Override
        public void restore(CountFamily family, Map<Long, Long> snapshot) {
            snapshot.forEach((id, delta) -> record(family, id, delta));
        }

        private Map<Long, Long> toIdMap(Map<String, Long> raw) {
            Map<Long, Long> result = new HashMap<>(raw.size());
            raw.forEach((field, count) -> {
                try {
                    result.put(Long.parseLong(field), count);
                } catch (NumberFormatException e) {
                    // 忽略无法解析为整数的 field
                }
            });
            return result;
        }
    }
}
