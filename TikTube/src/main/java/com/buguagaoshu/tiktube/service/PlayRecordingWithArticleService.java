package com.buguagaoshu.tiktube.service;

import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.buguagaoshu.tiktube.utils.PageUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-05-22 16:19
 */
public interface PlayRecordingWithArticleService {
    /**
     * 获取历史记录分页查询
     * */
    PageUtils playRecordingList(Map<String, Object> params, HttpServletRequest request);


    String savePlayLog(PlayRecordingEntity playRecording, HttpServletRequest request);
}
