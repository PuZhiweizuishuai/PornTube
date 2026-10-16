package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
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
     * 记录一次播放：写入缓冲，由定时任务批量 upsert 落库，请求线程不同步写数据库。
     * 同一 (用户, 稿件, 分P) 在缓冲窗口内只保留最新一次上报。
     *
     * @param videoTime 观看进度（秒），null 表示只刷新观看时间、不覆盖已记录的进度
     * */
    void saveHistory(FileTableEntity file, Long userId, String ua, Double videoTime);

    /**
     * 今日播放历史记录列表
     * */
    List<PlayRecordingEntity> todayPlayList(Long userId);


    int todayPlayCount(Long userId);
}

