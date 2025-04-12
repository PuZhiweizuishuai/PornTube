package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 播放记录
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface PlayRecordingService extends IService<PlayRecordingEntity> {

    /**
     * 获取历史记录
     * */
    IPage<PlayRecordingEntity> queryPage(Map<String, Object> params, HttpServletRequest request);


    /**
     * 查找播放记录使用 articleId
     * */
    PlayRecordingEntity findPlayRecordingEntityByArticleIdAndVideoId(Long articleId, Long videoId, Long userId);

    /**
     * 保存播放记录
     * @return ArticleId 需要增加播放量
     *         0 不需要增加
     * */
    long saveHistory(FileTableEntity file, Long userId, String ua);

    /**
     * 今日播放历史记录列表
     * */
    List<PlayRecordingEntity> todayPlayList(Long userId);


    int todayPlayCount(Long userId);
}

