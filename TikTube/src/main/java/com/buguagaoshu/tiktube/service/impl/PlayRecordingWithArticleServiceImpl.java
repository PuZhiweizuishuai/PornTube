package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.service.PlayRecordingService;
import com.buguagaoshu.tiktube.service.PlayRecordingWithArticleService;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.vo.PlayRecordingWithArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

    private final IpUtil ipUtil;


    @Autowired
    public PlayRecordingWithArticleServiceImpl(PlayRecordingService playRecordingService,
                                               ArticleService articleService,
                                               FileTableService fileTableService,
                                               IpUtil ipUtil) {
        this.playRecordingService = playRecordingService;
        this.articleService = articleService;
        this.fileTableService = fileTableService;
        this.ipUtil = ipUtil;
    }


    @Override
    public PageUtils playRecordingList(Map<String, Object> params, HttpServletRequest request) {
        IPage<PlayRecordingEntity> page = playRecordingService.queryPage(params, request);
        List<PlayRecordingEntity> records = page.getRecords();
        if (records.isEmpty()) {
            return new PageUtils(Collections.emptyList(), page.getTotal(), page.getSize(), page.getCurrent());
        }

        Set<Long> aids = records.stream().map(PlayRecordingEntity::getArticleId).collect(Collectors.toSet());
        Map<Long, ArticleEntity> articleEntityMap = articleService.listByIds(aids).stream()
                .collect(Collectors.toMap(ArticleEntity::getId, Function.identity()));

        List<PlayRecordingWithArticleVo> play = new ArrayList<>(records.size());
        for (PlayRecordingEntity playRecordingEntity : records) {
            ArticleEntity articleEntity = articleEntityMap.get(playRecordingEntity.getArticleId());
            // 稿件已删除时跳过：历史记录卡片直接取 articleEntity.id，带 null 进去会渲染报错
            if (articleEntity == null) {
                continue;
            }
            PlayRecordingWithArticleVo playRecordingWithArticleVo = new PlayRecordingWithArticleVo();
            playRecordingWithArticleVo.setPlayRecordingEntity(playRecordingEntity);
            playRecordingWithArticleVo.setArticleEntity(articleEntity);
            play.add(playRecordingWithArticleVo);
        }
        return new PageUtils(play, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public String savePlayLog(PlayRecordingEntity playRecording, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId(request);
        if (!userId.equals(playRecording.getUserId())) {
            return "用户信息错误";
        }
        FileTableEntity videoFile = fileTableService.getById(playRecording.getVideoId());
        if (videoFile == null) {
            return "视频信息错误";
        }
        // 上报的稿件必须属于这个分P，视频ID与稿件ID都以服务端查到的文件为准
        if (!Objects.equals(videoFile.getArticleId(), playRecording.getArticleId())) {
            return "no power";
        }
        playRecordingService.saveHistory(videoFile, userId, ipUtil.getUa(request), playRecording.getVideoTime());
        return "success";
    }
}
