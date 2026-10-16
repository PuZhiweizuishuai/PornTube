package com.buguagaoshu.tiktube.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buguagaoshu.tiktube.cache.HotCache;
import com.buguagaoshu.tiktube.cache.VideoCatalogCache;
import com.buguagaoshu.tiktube.dao.ArticleDao;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.utils.SQLFilter;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 相关推荐
 *
 * 召回与打分都在 {@link VideoCatalogCache} 的内存标签倒排索引里完成，请求路径不扫 article 表；
 * 只保留一次按主键批量取回展示字段的查询。候选不足时用热门榜快照补位，
 * 补位条目自带 sort 热度分，前端据此区分「相似」与「热门补位」。
 */
@Slf4j
@Component
public class ArticleRecommender {

    private final ArticleDao articleDao;
    private final VideoCatalogCache videoCatalogCache;
    private final HotCache hotCache;
    private final ArticleViewEnricher articleViewEnricher;
    private final ArticleVisibilityResolver visibilityResolver;

    @Autowired
    public ArticleRecommender(ArticleDao articleDao,
                              VideoCatalogCache videoCatalogCache,
                              HotCache hotCache,
                              ArticleViewEnricher articleViewEnricher,
                              ArticleVisibilityResolver visibilityResolver) {
        this.articleDao = articleDao;
        this.videoCatalogCache = videoCatalogCache;
        this.hotCache = hotCache;
        this.articleViewEnricher = articleViewEnricher;
        this.visibilityResolver = visibilityResolver;
    }

    /**
     * 按源稿件的标签与分区推荐相似视频，候选不足时用热门榜补位
     *
     * @param articleId 源稿件 ID，它自己不会出现在结果里
     * @param limit     期望条数
     */
    public List<ArticleViewData> byArticleId(Long articleId, int limit) {
        if (articleId == null || articleId <= 0 || limit <= 0) {
            return new ArrayList<>();
        }
        ArticleEntity article = articleDao.selectById(articleId);
        if (article == null) {
            return new ArrayList<>();
        }
        return recommend(ArticleEntityFactory.parseTag(article.getTag()), article.getCategory(), articleId, limit);
    }

    /**
     * @param sourceCategory 源稿件分区，为 null 时跳过同分区加分这一路信号
     */
    private List<ArticleViewData> recommend(List<String> tags, Integer sourceCategory,
                                            Long excludeArticleId, int limit) {
        Set<Long> picked = new HashSet<>();
        picked.add(excludeArticleId);

        List<ArticleViewData> result = new ArrayList<>(limit);
        List<String> sourceTags = tags == null ? List.of() : tags;
        if (!sourceTags.isEmpty()) {
            result.addAll(loadRankedVideos(
                    videoCatalogCache.recommend(sourceTags, sourceCategory, excludeArticleId, limit), picked));

            // 只在进程刚启动、索引还没建好时走这条扫表路径
            List<String> tagPatterns = videoCatalogCache.isReady() ? List.of() : buildTagLikePatterns(sourceTags);
            if (!tagPatterns.isEmpty()) {
                result.addAll(loadVideos(articleDao.findSimilarArticlesByTags(
                        tagPatterns, excludeArticleId,
                        limit - result.size(), FileTypeEnum.VIDEO.getCode()), picked));
            }
        }

        if (result.size() < limit) {
            result.addAll(hotPadding(hotCache.getHotList(), limit - result.size(), picked));
        }
        return result;
    }

    /**
     * 按召回顺序取回展示字段，一次主键批量查询
     */
    private List<ArticleViewData> loadRankedVideos(List<Long> rankedIds, Set<Long> picked) {
        if (rankedIds.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", rankedIds);

        Map<Long, ArticleEntity> videosById = new HashMap<>(rankedIds.size() * 2);
        for (ArticleEntity video : articleDao.selectList(wrapper)) {
            videosById.put(video.getId(), video);
        }

        List<ArticleEntity> ordered = new ArrayList<>(rankedIds.size());
        for (Long id : rankedIds) {
            ArticleEntity video = videosById.get(id);
            if (video != null && isListable(video) && picked.add(id)) {
                ordered.add(video);
            }
        }
        return articleViewEnricher.enrich(ordered);
    }

    private List<ArticleViewData> loadVideos(List<ArticleEntity> videos, Set<Long> picked) {
        List<ArticleEntity> kept = new ArrayList<>(videos.size());
        for (ArticleEntity video : videos) {
            if (isListable(video) && picked.add(video.getId())) {
                kept.add(video);
            }
        }
        return articleViewEnricher.enrich(kept);
    }

    /**
     * 热门榜补位，条目本身的 sort 就是热度分
     */
    private static List<ArticleViewData> hotPadding(List<ArticleViewData> hotList, int need, Set<Long> picked) {
        List<ArticleViewData> padding = new ArrayList<>(need);
        for (ArticleViewData hot : hotList) {
            if (padding.size() >= need) {
                break;
            }
            if (hot.getId() != null && picked.add(hot.getId())) {
                padding.add(hot);
            }
        }
        return padding;
    }

    /**
     * 标签在库里存成 {@code ["a","b"]} 形式的 JSON 数组，匹配模式带上引号，
     * 这样「小猫」不会命中「猫」
     */
    private static List<String> buildTagLikePatterns(List<String> tags) {
        List<String> patterns = new ArrayList<>(tags.size());
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            patterns.add("%\"" + SQLFilter.escapeLike(tag.trim()) + "\"%");
        }
        return patterns;
    }

    private boolean isListable(ArticleEntity article) {
        return visibilityResolver.isPubliclyListed(article, FileTypeEnum.VIDEO.getCode());
    }
}
