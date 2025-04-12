package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.enums.ExamineTypeEnum;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.vo.FavoritesAndArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.FavoritesTableDao;
import com.buguagaoshu.tiktube.entity.FavoritesTableEntity;
import com.buguagaoshu.tiktube.service.FavoritesTableService;
import org.springframework.transaction.annotation.Transactional;


@Service("favoritesTableService")
public class FavoritesTableServiceImpl extends ServiceImpl<FavoritesTableDao, FavoritesTableEntity> implements FavoritesTableService {

    final ArticleService articleService;

    @Autowired
    public FavoritesTableServiceImpl(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<FavoritesTableEntity> page = this.page(
                new Query<FavoritesTableEntity>().getPage(params),
                new QueryWrapper<FavoritesTableEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoritesTableEntity toggleFavorites(FavoritesTableEntity favorite, Long userId) {
        QueryWrapper<FavoritesTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("artice_id", favorite.getArticeId());
        FavoritesTableEntity one = this.getOne(wrapper);
        // 没有收藏过，增加收藏
        if (one == null) {
            FavoritesTableEntity favoritesTableEntity = new FavoritesTableEntity();
            favoritesTableEntity.setUserId(userId);
            favoritesTableEntity.setArticeId(favorite.getArticeId());
            favoritesTableEntity.setCreateTime(System.currentTimeMillis());
            // TODO 待实现收藏标签
            favoritesTableEntity.setFavoritesLabelId(null);
            this.save(favoritesTableEntity);
            articleService.addCount("favorite_count", favorite.getArticeId(), 1);
            return favoritesTableEntity;
        } else {
            // 取消收藏
            this.removeById(one);
            articleService.addCount("favorite_count", one.getArticeId(), -1);
            return favorite;
        }
    }

    @Override
    public boolean checkFavorites(Long articleId, Long userId) {
        QueryWrapper<FavoritesTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("artice_id",articleId);
        FavoritesTableEntity one = this.getOne(wrapper);
        return one != null;
    }

    @Override
    public PageUtils userFavorites(Map<String, Object> params, Long userId) {
        QueryWrapper<FavoritesTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        IPage<FavoritesTableEntity> page = this.page(
                new Query<FavoritesTableEntity>().getPage(params),
                wrapper
        );
        if (page.getTotal() == 0) {
            return new PageUtils(page);
        }
        Set<Long> articleIds = page.getRecords().stream().map(FavoritesTableEntity::getArticeId).collect(Collectors.toSet());
        List<ArticleEntity> articleEntities = articleService.listByIds(articleIds);
        Map<Long, ArticleEntity> map = new HashMap<>();
        for (ArticleEntity articleEntity : articleEntities) {
            articleEntity.setUa("");
            articleEntity.setIp("");
            articleEntity.setCity("");
            articleEntity.setExamineUser(null);
            articleEntity.setExamineMessage("");
            if (articleEntity.getExamineStatus().equals(ExamineTypeEnum.SUCCESS.getCode()) && articleEntity.getStatus().equals(ArticleStatusEnum.NORMAL.getCode())) {
                map.put(articleEntity.getId(), articleEntity);
            }
        }
        List<FavoritesAndArticle> favoritesAndArticleList = new ArrayList<>();
        for (FavoritesTableEntity favoritesTableEntity : page.getRecords()) {
            FavoritesAndArticle favoritesAndArticle = new FavoritesAndArticle();
            favoritesAndArticle.setFavorite(favoritesTableEntity);
            favoritesAndArticle.setArticleEntity(map.get(favoritesTableEntity.getArticeId()));
            favoritesAndArticleList.add(favoritesAndArticle);
        }
        return new PageUtils(favoritesAndArticleList, page.getTotal(), page.getSize(), page.getCurrent());
    }

}