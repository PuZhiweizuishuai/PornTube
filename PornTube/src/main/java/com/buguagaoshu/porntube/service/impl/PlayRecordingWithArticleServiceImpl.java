package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.entity.PlayRecordingEntity;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.service.PlayRecordingService;
import com.buguagaoshu.porntube.service.PlayRecordingWithArticleService;
import com.buguagaoshu.porntube.utils.IpUtil;
import com.buguagaoshu.porntube.utils.JwtUtil;
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

    private final FileTableService fileTableService;

    @Autowired
    public PlayRecordingWithArticleServiceImpl(PlayRecordingService playRecordingService, ArticleService articleService, FileTableService fileTableService) {
        this.playRecordingService = playRecordingService;
        this.articleService = articleService;
        this.fileTableService = fileTableService;
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

    @Override
    public String savePlayLog(PlayRecordingEntity playRecording, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        if (userId == playRecording.getUserId()) {
            FileTableEntity videoFile = fileTableService.getById(playRecording.getVideoId());
            if (videoFile == null) {
                return "视频信息错误";
            }
            if (playRecording.getArticleId().equals(videoFile.getArticleId())) {
                PlayRecordingEntity nowLog =
                        playRecordingService.findPlayRecordingEntityByArticleIdAndVideoId(videoFile.getArticleId(), videoFile.getId(), userId);
                if (nowLog == null) {
                    playRecording.setUpdateTime(System.currentTimeMillis());
                    playRecording.setUa(IpUtil.getUa(request));
                    playRecordingService.save(playRecording);
                } else {
                    nowLog.setUa(IpUtil.getUa(request));
                    nowLog.setVideoTime(playRecording.getVideoTime());
                    nowLog.setUpdateTime(System.currentTimeMillis());
                    playRecordingService.updateById(nowLog);
                }
                return "success";
            }
            return "no power";
        } else {
            return "用户信息错误";
        }
    }
}
