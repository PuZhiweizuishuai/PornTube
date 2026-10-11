package com.buguagaoshu.tiktube.support;

import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.dto.ArticleDraft;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.enums.ExamineTypeEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 稿件实体构建器
 *
 * 背景：buildArticleEntity / updateArticleEntity 在
 * ArticleServiceImpl（视频）与 ArticleTextService（文本）各写一遍，
 * 标签序列化、时间、审核状态、计数初始化等 ~50 行逻辑完全重复。
 * 现在通过 {@link ArticleDraft} 统一入口，两处调用方只负责设置各自的类型专属字段
 * （视频的时长/像素/帧率、文本的类型标记）。
 */
@Slf4j
@Component
public class ArticleEntityFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WebSettingCache webSettingCache;

    @Autowired
    public ArticleEntityFactory(WebSettingCache webSettingCache) {
        this.webSettingCache = webSettingCache;
    }

    /**
     * 构建新稿件实体（不含类型专属字段）
     *
     * @param draft  投稿表单
     * @param userId 投稿人
     * @param imgUrl 封面地址，为 null 时写空串
     */
    public ArticleEntity create(ArticleDraft draft, long userId, String imgUrl) {
        ArticleEntity article = new ArticleEntity();
        applyCommon(article, draft, imgUrl, true);
        article.setUserId(userId);

        long now = System.currentTimeMillis();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setExamineStatus(resolveExamineStatus(false));
        initCounters(article);
        article.setStatus(ArticleStatusEnum.NORMAL.getCode());
        return article;
    }

    /**
     * 把投稿表单的改动应用到已有稿件上（不含类型专属字段）
     *
     * @param imgUrl 新封面地址，为 null 或空时保留原封面
     */
    public void applyUpdate(ArticleEntity article, ArticleDraft draft, String imgUrl) {
        applyCommon(article, draft, imgUrl, false);
        article.setUpdateTime(System.currentTimeMillis());
        // 修改后一律重新进入审核
        article.setExamineStatus(ExamineTypeEnum.PENDING_REVIEW.getCode());
    }

    /**
     * 计算审核状态：开启审核 → 待审核，否则直接通过
     */
    public int resolveExamineStatus(boolean forcePending) {
        if (forcePending) {
            return ExamineTypeEnum.PENDING_REVIEW.getCode();
        }
        if (webSettingCache.getWebConfigData().getOpenExamine()) {
            return ExamineTypeEnum.PENDING_REVIEW.getCode();
        }
        return ExamineTypeEnum.SUCCESS.getCode();
    }

    /** 标签序列化（失败时写空串，不让投稿流程中断） */
    public String serializeTag(List<String> tag) {
        try {
            return OBJECT_MAPPER.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            log.warn("稿件标签序列化失败: {}", e.getMessage());
            return "";
        }
    }

    private void applyCommon(ArticleEntity article, ArticleDraft draft, String imgUrl, boolean creating) {
        article.setTitle(draft.getTitle());
        article.setDescribes(draft.getDescribe());
        article.setCategory(draft.getCategory());
        if (creating) {
            // 新建时封面为空写空串（字段为 NOT NULL）
            article.setImgUrl(imgUrl == null ? "" : imgUrl);
        } else if (StringUtils.hasText(imgUrl)) {
            // 修改时允许「不传封面」表示保留原封面
            article.setImgUrl(imgUrl);
        }
        article.setTag(serializeTag(draft.getTag()));
    }

    private void initCounters(ArticleEntity article) {
        article.setViewCount(0L);
        article.setLikeCount(0L);
        article.setFavoriteCount(0L);
        article.setDislikeCount(0L);
        article.setCommentCount(0L);
        article.setDanmakuCount(0L);
    }
}
