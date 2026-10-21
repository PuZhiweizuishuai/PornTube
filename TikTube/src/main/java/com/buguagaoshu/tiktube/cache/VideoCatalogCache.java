package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.dao.ArticleDao;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.support.ArticleEntityFactory;
import com.buguagaoshu.tiktube.support.VideoRanking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 视频标签倒排索引缓存
 *
 * 背景：相关推荐原先每次请求都执行 {@code tag LIKE '%x%' OR tag LIKE '%y%'}，
 * 前置通配符用不上索引，等于每次播放页访问都全表扫描一遍 article。
 * 现在把参与打分的字段常驻内存，播放页推荐只在内存里召回打分，
 * 只保留一次按主键批量取回展示字段的查询。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@Slf4j
@Component
public class VideoCatalogCache {

    /**
     * 索引的视频条数上限，按发布时间倒序取最近的稿件，
     * 太老的长尾视频不在索引里时由 SQL 回退路径兜底
     */
    private static final int MAX_INDEXED_VIDEOS = 50_000;

    /**
     * 单个标签的倒排链长度上限，召回规模因此有上界：标签数 × 本值
     */
    private static final int MAX_POSTING_LIST_SIZE = 120;

    /**
     * 同一作者在一份推荐列表里最多出现几条，避免整屏都是同一个人的稿件
     */
    private static final int MAX_PER_UPLOADER = 2;

    /**
     * 参与打分的源稿件标签数量上限，标签特别多的稿件只取前几个
     */
    private static final int MAX_SOURCE_TAGS = 10;

    private final ArticleDao articleDao;

    /**
     * 整体替换的不可变快照，读侧不需要加锁
     */
    private volatile Catalog catalog = Catalog.EMPTY;

    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    @Autowired
    public VideoCatalogCache(ArticleDao articleDao) {
        this.articleDao = articleDao;
    }

    /**
     * 重建索引，同一时刻只允许一个构建任务在跑。
     * 索引是纯派生数据、各实例每两小时本来就会各自重建一次，所以不进跨实例版本同步：
     * 广播它只会让管理员一次手动刷新触发所有实例同时全表投影扫描
     */
    public void refresh() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        long start = System.currentTimeMillis();
        try {
            List<ArticleEntity> videos =
                    articleDao.findVideosForTagIndex(FileTypeEnum.VIDEO.getCode(), MAX_INDEXED_VIDEOS);
            catalog = buildCatalog(videos, start);
            log.info("视频标签索引构建完成：稿件 {} 篇，标签 {} 个，耗时 {} ms",
                    catalog.totalDocuments(), catalog.tagDocumentFrequency().size(),
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("视频标签索引构建失败，继续使用上一份索引", e);
        } finally {
            refreshing.set(false);
        }
    }

    public boolean isReady() {
        return catalog.totalDocuments() > 0;
    }

    /**
     * 内存召回并打分，返回排好序的视频 ID，不足 limit 条时由调用方补热门
     *
     * @param sourceTags        源稿件标签
     * @param sourceCategory    源稿件分区，可为 null
     * @param excludeArticleId  需要排除的稿件 ID（就是源稿件自己）
     * @param limit             返回条数
     */
    public List<Long> recommend(List<String> sourceTags, Integer sourceCategory,
                               Long excludeArticleId, int limit) {
        Catalog current = catalog;
        if (limit <= 0 || current.totalDocuments() <= 0
                || sourceTags == null || sourceTags.isEmpty()) {
            return List.of();
        }

        String[] source = distinctTags(sourceTags);
        if (source.length == 0) {
            return List.of();
        }

        List<Doc> candidates = recall(current, source, excludeArticleId);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> takenPerUploader = new HashMap<>();
        List<Scored> scored = new ArrayList<>(candidates.size());
        for (Doc candidate : candidates) {
            double similarity = VideoRanking.tagCosine(source, candidate.tags(), tag -> idf(current, tag));
            if (similarity <= 0) {
                continue;
            }
            double trend = VideoRanking.normalizeTrend(candidate.trendScore(), current.maxTrendScore());
            boolean sameCategory = sourceCategory != null && sourceCategory == candidate.category();
            scored.add(new Scored(candidate.id(),
                    VideoRanking.recommendScore(similarity, trend, candidate.qualityRate(), sameCategory),
                    candidate.userId()));
        }
        scored.sort(Comparator.comparingDouble((Scored item) -> item.score()).reversed());

        List<Long> result = new ArrayList<>(limit);
        for (Scored item : scored) {
            int taken = takenPerUploader.merge(item.userId(), 1, Integer::sum);
            if (taken > MAX_PER_UPLOADER) {
                continue;
            }
            result.add(item.id());
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    /**
     * 合并各标签倒排链的前段，同一稿件只保留一次
     */
    private List<Doc> recall(Catalog current, String[] sourceTags, Long excludeArticleId) {
        Map<Long, Doc> recalled = new HashMap<>();
        for (String tag : sourceTags) {
            List<Doc> postings = current.tagPostings().get(tag);
            if (postings == null) {
                continue;
            }
            for (Doc doc : postings) {
                if (excludeArticleId != null && doc.id() == excludeArticleId) {
                    continue;
                }
                recalled.putIfAbsent(doc.id(), doc);
            }
        }
        return new ArrayList<>(recalled.values());
    }

    private double idf(Catalog current, String tag) {
        return VideoRanking.idf(current.totalDocuments(),
                current.tagDocumentFrequency().getOrDefault(tag, 0L));
    }

    private Catalog buildCatalog(List<ArticleEntity> videos, long now) {
        Map<String, List<Doc>> postings = new HashMap<>();
        double maxTrendScore = 0;
        int indexedDocuments = 0;

        for (ArticleEntity video : videos) {
            String[] tags = distinctTags(ArticleEntityFactory.parseTag(video.getTag()));
            if (tags.length == 0 || video.getId() == null) {
                continue;
            }
            double trendScore = VideoRanking.trendScore(video, now);
            Doc doc = new Doc(video.getId(), tags,
                    video.getCategory() == null ? 0 : video.getCategory(),
                    video.getUserId() == null ? 0 : video.getUserId(),
                    trendScore,
                    VideoRanking.qualityRate(video));
            indexedDocuments++;
            maxTrendScore = Math.max(maxTrendScore, trendScore);
            for (String tag : tags) {
                postings.computeIfAbsent(tag, key -> new ArrayList<>()).add(doc);
            }
        }

        Map<String, Long> documentFrequency = new HashMap<>(postings.size() * 2);
        postings.forEach((tag, postingList) -> {
            // 词频记的是裁剪前的真实篇数，倒排链只留热度最高的前段
            documentFrequency.put(tag, (long) postingList.size());
            postingList.sort(Comparator.comparingDouble(Doc::trendScore).reversed());
            if (postingList.size() > MAX_POSTING_LIST_SIZE) {
                postingList.subList(MAX_POSTING_LIST_SIZE, postingList.size()).clear();
            }
        });

        return new Catalog(Map.copyOf(postings), Map.copyOf(documentFrequency), indexedDocuments, maxTrendScore);
    }

    /**
     * 去重并截断，保持原有顺序
     */
    private String[] distinctTags(List<String> tags) {
        Set<String> distinct = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            distinct.add(tag.trim());
            if (distinct.size() >= MAX_SOURCE_TAGS) {
                break;
            }
        }
        return distinct.toArray(String[]::new);
    }

    private record Doc(long id, String[] tags, long category, long userId,
                       double trendScore, double qualityRate) {
    }

    private record Scored(long id, double score, long userId) {
    }

    private record Catalog(Map<String, List<Doc>> tagPostings,
                           Map<String, Long> tagDocumentFrequency,
                           long totalDocuments,
                           double maxTrendScore) {

        static final Catalog EMPTY = new Catalog(Map.of(), Map.of(), 0, 0);
    }
}
