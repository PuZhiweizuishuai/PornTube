package com.buguagaoshu.tiktube.schedule;

import com.buguagaoshu.tiktube.cache.AdsCountRecorder;
import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.CountRecorder.CountFamily;
import com.buguagaoshu.tiktube.cache.PlayCountRecorder;
import com.buguagaoshu.tiktube.cache.PlayRecordBuffer;
import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.dao.AiConfigDao;
import com.buguagaoshu.tiktube.dao.ArticleDao;
import com.buguagaoshu.tiktube.dao.CommentDao;
import com.buguagaoshu.tiktube.dao.PlayRecordingDao;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.buguagaoshu.tiktube.repository.APICurrentLimitingRepository;
import com.buguagaoshu.tiktube.service.AIConfigServer;
import com.buguagaoshu.tiktube.service.AdvertisementService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-22
 * 定期将计数缓存批量同步到数据库。
 * <p>
 * 每个计数族独立小事务、独立失败回退：某一族落库失败只并回该族缓冲，不影响其他族；
 * 缓冲通过 drain 原子换出，落库期间产生的新计数写入新缓冲，不存在丢数窗口。
 */
@Component
@Slf4j
public class CountTasks {
    private final PlayCountRecorder playCountRecorder;

    private final PlayRecordBuffer playRecordBuffer;

    private final CountRecorder countRecorder;

    private final AdsCountRecorder adsCountRecorder;

    private final ArticleDao articleDao;

    private final CommentDao commentDao;

    private final AiConfigDao aiConfigDao;

    private final PlayRecordingDao playRecordingDao;

    private final AIConfigServer aiConfigServer;

    private final AdvertisementService advertisementService;

    private final APICurrentLimitingRepository apiCurrentLimitingRepository;

    private final TransactionTemplate transactionTemplate;

    private final MyConfigProperties configProperties;

    @Autowired
    public CountTasks(PlayCountRecorder playCountRecorder,
                      PlayRecordBuffer playRecordBuffer,
                      CountRecorder countRecorder,
                      AdsCountRecorder adsCountRecorder,
                      ArticleDao articleDao,
                      CommentDao commentDao,
                      AiConfigDao aiConfigDao,
                      PlayRecordingDao playRecordingDao,
                      AIConfigServer aiConfigServer,
                      AdvertisementService advertisementService,
                      APICurrentLimitingRepository apiCurrentLimitingRepository,
                      TransactionTemplate transactionTemplate,
                      MyConfigProperties configProperties) {
        this.playCountRecorder = playCountRecorder;
        this.playRecordBuffer = playRecordBuffer;
        this.countRecorder = countRecorder;
        this.adsCountRecorder = adsCountRecorder;
        this.articleDao = articleDao;
        this.commentDao = commentDao;
        this.aiConfigDao = aiConfigDao;
        this.playRecordingDao = playRecordingDao;
        this.aiConfigServer = aiConfigServer;
        this.advertisementService = advertisementService;
        this.apiCurrentLimitingRepository = apiCurrentLimitingRepository;
        this.transactionTemplate = transactionTemplate;
        this.configProperties = configProperties;
    }

    /**
     * 默认 5 分钟一轮，可经 tiktube.count-sync-interval-millis 调整
     */
    @Scheduled(fixedDelayString = "${tiktube.count-sync-interval-millis:300000}",
            initialDelayString = "${tiktube.count-sync-initial-delay-millis:30000}")
    public void saveCount() {
        flushAll();
    }

    /**
     * 关停前做最后一次刷盘，把缓冲里剩余的增量落库
     */
    @PreDestroy
    public void flushOnShutdown() {
        log.info("应用关停，执行计数缓存最终刷盘");
        flushPlayRecords();
        flushAll();
    }

    /**
     * 播放记录缓冲落库。观看请求只写内存，这里默认 60 秒批量 upsert 一次，
     * 频率与计数族解耦：历史记录与续播进度最多滞后一个间隔
     */
    @Scheduled(fixedDelayString = "${tiktube.play-record-flush-interval-millis:60000}",
            initialDelayString = "${tiktube.play-record-flush-interval-millis:60000}")
    public void savePlayRecords() {
        flushPlayRecords();
    }

    /**
     * 限流记录清理与计数落库无关，保持原有的 6 小时节奏独立调度
     */
    @Scheduled(fixedRate = 21600000)
    public void clearApiLimitRecords() {
        apiCurrentLimitingRepository.clearAllRecords();
    }

    private void flushAll() {
        flush("article.view_count", playCountRecorder.drainPlayMap(),
                chunk -> articleDao.batchUpdateCount("view_count", chunk),
                playCountRecorder::restorePlayMap);

        flushArticleFamily(CountFamily.ARTICLE_COMMENT, "comment_count");
        flushArticleFamily(CountFamily.ARTICLE_LIKE, "like_count");
        flushArticleFamily(CountFamily.ARTICLE_FAVORITE, "favorite_count");
        flushArticleFamily(CountFamily.ARTICLE_DISLIKE, "dislike_count");
        flushArticleFamily(CountFamily.DANMAKU, "danmaku_count");

        flushCommentFamily(CountFamily.COMMENT_COUNT, "comment_count");
        flushCommentFamily(CountFamily.COMMENT_LIKE, "like_count");
        flushCommentFamily(CountFamily.COMMENT_DISLIKE, "dislike_count");

        flush("advertisement.view_count", adsCountRecorder.drainAdsMap(),
                chunk -> advertisementService.updateViewCounts(chunk),
                adsCountRecorder::restoreAdsMap);

        flushAiToken();
    }

    private void flushArticleFamily(CountFamily family, String column) {
        flush("article." + column, countRecorder.drain(family),
                chunk -> articleDao.batchUpdateCount(column, chunk),
                snapshot -> countRecorder.restore(family, snapshot));
    }

    private void flushCommentFamily(CountFamily family, String column) {
        flush("comment." + column, countRecorder.drain(family),
                chunk -> commentDao.batchUpdateCount(column, chunk),
                snapshot -> countRecorder.restore(family, snapshot));
    }

    private void flushAiToken() {
        Map<Long, Long> snapshot = countRecorder.drain(CountFamily.AI_TOKEN);
        if (snapshot.isEmpty()) {
            return;
        }
        // 取走之后到配置快照刷新之前，这部分 token 既不在缓冲也不在库里，
        // 挂成 in-flight 让配额判断继续看见它，否则这个窗口里会超支
        countRecorder.holdAiTokenInFlight(snapshot);
        try {
            transactionTemplate.executeWithoutResult(status ->
                    forEachChunk(snapshot, chunk -> aiConfigDao.batchUpdateCount("use_tokens", chunk)));
            // 缓存重建是非事务副作用，必须等落库提交成功后再做
            aiConfigServer.setCacheConfig();
        } catch (Exception e) {
            countRecorder.restore(CountFamily.AI_TOKEN, snapshot);
            log.error("同步 AI Token 计数失败，{} 条已并回缓冲，下一轮重试", snapshot.size(), e);
        } finally {
            countRecorder.releaseAiTokenInFlight(snapshot);
        }
    }

    /**
     * 播放记录落库：缓冲里每个 (用户, 稿件, 分P) 只剩最新一条，幂等 upsert 后失败整批并回缓冲
     */
    private void flushPlayRecords() {
        List<PlayRecordingEntity> snapshot = playRecordBuffer.drain();
        if (snapshot.isEmpty()) {
            return;
        }
        try {
            transactionTemplate.executeWithoutResult(status ->
                    forEachChunk(snapshot, configProperties.getPlayRecordBatchSize(),
                            playRecordingDao::batchUpsert));
            log.debug("播放记录同步完成，共 {} 条", snapshot.size());
        } catch (Exception e) {
            playRecordBuffer.restore(snapshot);
            log.error("播放记录同步失败，{} 条已并回缓冲，下一轮重试", snapshot.size(), e);
        }
    }

    /**
     * 单个计数域的落库：独立小事务，失败只回滚本域并把快照并回缓冲
     */
    private <K> void flush(String domain, Map<K, Long> snapshot,
                           Consumer<Map<K, Long>> writer, Consumer<Map<K, Long>> rollback) {
        if (snapshot.isEmpty()) {
            return;
        }
        try {
            transactionTemplate.executeWithoutResult(status -> forEachChunk(snapshot, writer));
            log.debug("计数域 [{}] 同步完成，共 {} 条", domain, snapshot.size());
        } catch (Exception e) {
            rollback.accept(snapshot);
            log.error("计数域 [{}] 同步失败，{} 条已并回缓冲，下一轮重试", domain, snapshot.size(), e);
        }
    }

    /**
     * 分片执行，避免单条 CASE WHEN 批量 UPDATE 过大
     */
    private <K> void forEachChunk(Map<K, Long> map, Consumer<Map<K, Long>> action) {
        int batchSize = Math.max(1, configProperties.getCountSyncBatchSize());
        if (map.size() <= batchSize) {
            action.accept(map);
            return;
        }
        Map<K, Long> chunk = new HashMap<>(batchSize);
        for (Map.Entry<K, Long> entry : map.entrySet()) {
            chunk.put(entry.getKey(), entry.getValue());
            if (chunk.size() >= batchSize) {
                action.accept(chunk);
                chunk = new HashMap<>(batchSize);
            }
        }
        if (!chunk.isEmpty()) {
            action.accept(chunk);
        }
    }

    /**
     * 列表分片执行，避免单条多值 INSERT 过大
     */
    private <T> void forEachChunk(List<T> list, int batchSize, Consumer<List<T>> action) {
        int size = Math.max(1, batchSize);
        if (list.size() <= size) {
            action.accept(list);
            return;
        }
        List<List<T>> chunks = new ArrayList<>((list.size() + size - 1) / size);
        for (int from = 0; from < list.size(); from += size) {
            chunks.add(list.subList(from, Math.min(from + size, list.size())));
        }
        chunks.forEach(action);
    }
}
