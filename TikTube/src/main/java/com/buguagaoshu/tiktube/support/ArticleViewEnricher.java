package com.buguagaoshu.tiktube.support;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.buguagaoshu.tiktube.cache.CategoryCache;
import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.PlayCountRecorder;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.CategoryEntity;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 稿件列表富化组件
 *
 * 背景：把 ArticleEntity 转成 ArticleViewData 这一套
 * 「同步计数 + 拷贝属性 + 补作者信息 + 补分类信息 + 叠加缓存播放量」
 * 原本在 ArticleServiceImpl 里写了两份（addUserInfo / addArticleCategory），
 * 而 addCategoryInfo 又在 ArticleTextService 里原样复制了一份。
 * 这里统一提供给所有列表接口使用，避免继续复制粘贴。
 */
@Slf4j
@Component
public class ArticleViewEnricher {

    private final UserService userService;
    private final CategoryCache categoryCache;
    private final CountRecorder countRecorder;
    private final PlayCountRecorder playCountRecorder;

    @Autowired
    public ArticleViewEnricher(UserService userService,
                               CategoryCache categoryCache,
                               CountRecorder countRecorder,
                               PlayCountRecorder playCountRecorder) {
        this.userService = userService;
        this.categoryCache = categoryCache;
        this.countRecorder = countRecorder;
        this.playCountRecorder = playCountRecorder;
    }

    /**
     * 富化分页结果（带作者信息 + 分类信息），等价于原 addUserInfo(IPage)
     */
    public PageUtils enrichPage(IPage<ArticleEntity> page) {
        List<ArticleViewData> list = enrich(page.getRecords());
        return new PageUtils(list, page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 富化列表（带作者信息 + 分类信息），等价于原 addUserInfo(List)
     */
    public List<ArticleViewData> enrich(List<ArticleEntity> articles) {
        if (articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = articles.stream()
                .map(ArticleEntity::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询，避免 N+1
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIds);
        Map<Integer, CategoryEntity> categoryMap = categoryCache.getCategoryEntityMap();

        return articles.parallelStream().map(article -> {
            ArticleViewData viewData = new ArticleViewData();
            countRecorder.syncArticleCount(article);
            BeanUtils.copyProperties(article, viewData);

            UserEntity userEntity = userEntityMap.get(article.getUserId());
            if (userEntity != null) {
                viewData.setUsername(userEntity.getUsername());
                viewData.setAvatarUrl(userEntity.getAvatarUrl());
            }

            applyCategory(viewData, article.getCategory(), categoryMap);
            viewData.setViewCount(viewData.getViewCount() + playCountRecorder.getPlayCount(article.getId()));
            return viewData;
        }).collect(Collectors.toList());
    }

    /**
     * 富化分页结果（仅分类信息，不带作者），等价于原 addArticleCategory(IPage)
     */
    public List<ArticleViewData> enrichPageWithCategory(IPage<ArticleEntity> page) {
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, CategoryEntity> categoryMap = categoryCache.getCategoryEntityMap();

        return page.getRecords().parallelStream()
                .map(article -> {
                    countRecorder.syncArticleCount(article);
                    article.setViewCount(
                            article.getViewCount() + playCountRecorder.getPlayCount(article.getId())
                    );
                    ArticleViewData viewData = new ArticleViewData();
                    BeanUtils.copyProperties(article, viewData);
                    applyCategory(viewData, article.getCategory(), categoryMap);
                    return viewData;
                })
                .collect(Collectors.toList());
    }

    /** 填充分类与父分类 */
    public void applyCategory(ArticleViewData viewData, Integer categoryId) {
        applyCategory(viewData, categoryId, categoryCache.getCategoryEntityMap());
    }

    private void applyCategory(ArticleViewData viewData, Integer categoryId, Map<Integer, CategoryEntity> categoryMap) {
        if (categoryId == null || categoryMap == null) {
            return;
        }
        CategoryEntity categoryEntity = categoryMap.get(categoryId);
        if (categoryEntity == null) {
            return;
        }
        viewData.setChildrenCategory(categoryEntity);
        if (categoryEntity.getFatherId() != null && categoryEntity.getFatherId() != 0) {
            viewData.setFatherCategory(categoryMap.get(categoryEntity.getFatherId()));
        }
    }

    /** 填充作者信息（详情页使用） */
    public void applyAuthor(ArticleViewData viewData, Long userId) {
        if (userId == null) {
            return;
        }
        UserEntity author = userService.getById(userId);
        if (author == null) {
            return;
        }
        viewData.setUsername(author.getUsername());
        viewData.setFollowCount(author.getFollowCount());
        viewData.setFansCount(author.getFansCount());
        viewData.setAvatarUrl(author.getAvatarUrl());
        viewData.setIntroduction(author.getIntroduction());
    }
}
