package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.PlayRecordingEntity;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.PlayRecordingService;
import com.buguagaoshu.porntube.service.PlayRecordingWithArticleService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.vo.PlayRecordingWithArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-05-22 16:21
 */
@Service
public class PlayRecordingWithArticleServiceImpl implements PlayRecordingWithArticleService {
    private final PlayRecordingService playRecordingService;

    private final ArticleService articleService;

    @Autowired
    public PlayRecordingWithArticleServiceImpl(PlayRecordingService playRecordingService, ArticleService articleService) {
        this.playRecordingService = playRecordingService;
        this.articleService = articleService;
    }


    @Override
    public PageUtils playRecordingList(Map<String, Object> params, HttpServletRequest request) {
        IPage<PlayRecordingEntity> page = playRecordingService.queryPage(params, request);
        Set<Long> aids = page.getRecords().stream().map(PlayRecordingEntity::getArticleId).collect(Collectors.toSet());
        List<ArticleEntity> articleEntities = articleService.listByIds(aids);
        List<PlayRecordingWithArticleVo> play = new LinkedList<>();
        if (articleEntities != null && articleEntities.size() != 0) {
            Map<Long, ArticleEntity> articleEntityMap =  articleEntities.stream().collect(Collectors.toMap(ArticleEntity::getId, a->a));

            for (PlayRecordingEntity playRecordingEntity : page.getRecords()) {
                PlayRecordingWithArticleVo playRecordingWithArticleVo = new PlayRecordingWithArticleVo();
                playRecordingWithArticleVo.setPlayRecordingEntity(playRecordingEntity);


                ArticleEntity articleEntity = articleEntityMap.get(playRecordingEntity.getArticleId());

                playRecordingWithArticleVo.setArticleEntity(articleEntity);
                play.add(playRecordingWithArticleVo);
            }
        }
        IPage<PlayRecordingWithArticleVo> iPage = new IPage<PlayRecordingWithArticleVo>() {
            @Override
            public List<OrderItem> orders() {
                return null;
            }

            @Override
            public List<PlayRecordingWithArticleVo> getRecords() {
                return play;
            }

            @Override
            public IPage<PlayRecordingWithArticleVo> setRecords(List<PlayRecordingWithArticleVo> records) {
                return null;
            }

            @Override
            public long getTotal() {
                return page.getTotal();
            }

            @Override
            public IPage<PlayRecordingWithArticleVo> setTotal(long total) {
                return null;
            }

            @Override
            public long getSize() {
                return page.getSize();
            }

            @Override
            public IPage<PlayRecordingWithArticleVo> setSize(long size) {
                return null;
            }

            @Override
            public long getCurrent() {
                return page.getCurrent();
            }

            @Override
            public IPage<PlayRecordingWithArticleVo> setCurrent(long current) {
                return null;
            }
        };
        return new PageUtils(iPage);
    }
}
