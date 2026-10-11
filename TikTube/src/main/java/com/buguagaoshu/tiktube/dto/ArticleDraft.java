package com.buguagaoshu.tiktube.dto;

import java.util.List;

/**
 * 投稿表单的公共字段
 *
 * 视频稿件 {@link VideoArticleDto} 与文本稿件 {@link TextArticleDto} 在
 * 「标题 / 简介 / 分区 / 标签 / 封面」这些字段上完全一致，
 * 原先 ArticleServiceImpl 与 ArticleTextService 各写了一份 buildArticleEntity /
 * updateArticleEntity（约 100 行重复）。
 * 抽出本接口后，实体构建统一交给
 * {@link com.buguagaoshu.tiktube.support.ArticleEntityFactory}。
 */
public interface ArticleDraft {

    String getTitle();

    String getDescribe();

    Integer getCategory();

    List<String> getTag();

    /** 封面地址，可能为 null（文本稿件允许无封面） */
    String getImgUrl();
}
