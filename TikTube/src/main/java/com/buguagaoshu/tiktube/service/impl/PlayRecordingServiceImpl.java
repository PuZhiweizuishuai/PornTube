package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.PlayRecordBuffer;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.Query;
import com.buguagaoshu.tiktube.dao.PlayRecordingDao;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.buguagaoshu.tiktube.service.PlayRecordingService;

import jakarta.servlet.http.HttpServletRequest;


@Service("playRecordingService")
public class PlayRecordingServiceImpl extends ServiceImpl<PlayRecordingDao, PlayRecordingEntity> implements PlayRecordingService {

    private final PlayRecordBuffer playRecordBuffer;

    @Autowired
    public PlayRecordingServiceImpl(PlayRecordBuffer playRecordBuffer) {
        this.playRecordBuffer = playRecordBuffer;
    }

    @Override
    public IPage<PlayRecordingEntity> queryPage(Map<String, Object> params, HttpServletRequest request) {

        Long userId = JwtUtil.getUserId(request);
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("update_time");

        return this.page(
                new Query<PlayRecordingEntity>().getPage(params),
                wrapper
        );
    }

    @Override
    public void saveHistory(FileTableEntity file, Long userId, String ua, Double videoTime) {
        playRecordBuffer.record(file.getArticleId(), file.getId(), userId, videoTime, ua);
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
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.ge("update_time", toDayZero());
        return Math.toIntExact(this.count(wrapper));
    }


    public static long toDayZero() {
        long current = System.currentTimeMillis();
        return current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
    }
}
