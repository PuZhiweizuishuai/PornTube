package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.utils.JwtUtil;
import com.buguagaoshu.porntube.vo.PlayRecordingWithArticleVo;
import com.buguagaoshu.porntube.vo.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;
import com.buguagaoshu.porntube.dao.PlayRecordingDao;
import com.buguagaoshu.porntube.entity.PlayRecordingEntity;
import com.buguagaoshu.porntube.service.PlayRecordingService;

import javax.servlet.http.HttpServletRequest;


@Service("playRecordingService")
public class PlayRecordingServiceImpl extends ServiceImpl<PlayRecordingDao, PlayRecordingEntity> implements PlayRecordingService {

    private ArticleService articleService;

    @Autowired
    public void setArticleDao(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        Long userId = Long.parseLong(user.getId());
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("update_time");

        IPage<PlayRecordingEntity> page = this.page(
                new Query<PlayRecordingEntity>().getPage(params),
                wrapper
        );
        // Map<Long, PlayRecordingEntity> aid = page.getRecords().stream().collect(Collectors.toMap(PlayRecordingEntity::getArticleId, p -> p));

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

    @Override
    public PlayRecordingEntity findPlayRecordingEntityByArticleIdAndVideoId(Long articleId, Long videoId, Long userId) {
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.eq("video_id", videoId);
        wrapper.eq("user_id", userId);
        return this.getOne(wrapper);
    }

    @Override
    public void saveHistory(FileTableEntity file, Claims user, String ua) {
        long userId = Long.parseLong(user.getId());
        long time = System.currentTimeMillis();
        PlayRecordingEntity playRecordingEntity = findPlayRecordingEntityByArticleIdAndVideoId(file.getArticleId(), file.getId(), userId);
        if (playRecordingEntity != null) {
            playRecordingEntity.setUpdateTime(time);
            playRecordingEntity.setUa(ua);
            this.updateById(playRecordingEntity);
        } else {
            playRecordingEntity = new PlayRecordingEntity();
            playRecordingEntity.setArticleId(file.getArticleId());
            playRecordingEntity.setCreateTime(time);
            playRecordingEntity.setUpdateTime(time);
            playRecordingEntity.setUserId(userId);
            playRecordingEntity.setVideoId(file.getId());
            playRecordingEntity.setUa(ua);
            // TODO 采用缓存
            articleService.addViewCount(file.getArticleId(), 1L);
            // TODO 播放时间戳，视频个数
            this.save(playRecordingEntity);
        }
    }

    @Override
    public List<PlayRecordingEntity> todayPlayList(Long userId) {
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.ge("update_time", toDayZero());
        return this.list(wrapper);
    }

    @Override
    public int todayPlayCount(Long userId) {
        List<PlayRecordingEntity> l = todayPlayList(userId);
        if (l == null) {
            return  0;
        }
        return l.size();
    }


    public static long toDayZero() {
        long current = System.currentTimeMillis();
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        return zero;
    }


    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date(toDayZero())));
    }
}