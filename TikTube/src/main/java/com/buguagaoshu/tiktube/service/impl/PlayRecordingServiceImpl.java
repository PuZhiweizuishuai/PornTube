package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.Query;
import com.buguagaoshu.tiktube.dao.PlayRecordingDao;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.buguagaoshu.tiktube.service.PlayRecordingService;

import jakarta.servlet.http.HttpServletRequest;


@Service("playRecordingService")
public class PlayRecordingServiceImpl extends ServiceImpl<PlayRecordingDao, PlayRecordingEntity> implements PlayRecordingService {

    /**
     * 为写入播放记录方法加锁
     * */
    private final Lock lock = new ReentrantLock();


    @Override
    public IPage<PlayRecordingEntity> queryPage(Map<String, Object> params, HttpServletRequest request) {

        Long userId = JwtUtil.getUserId(request);
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("update_time");

        IPage<PlayRecordingEntity> page = this.page(
                new Query<PlayRecordingEntity>().getPage(params),
                wrapper
        );
        // Map<Long, PlayRecordingEntity> aid = page.getRecords().stream().collect(Collectors.toMap(PlayRecordingEntity::getArticleId, p -> p));

        return page;
    }

    @Override
    public PlayRecordingEntity findPlayRecordingEntityByArticleIdAndVideoId(Long articleId, Long videoId, Long userId) {
        QueryWrapper<PlayRecordingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.eq("video_id", videoId);
        wrapper.eq("user_id", userId);
        List<PlayRecordingEntity> list = list(wrapper);
        if (list != null && list.size() != 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public long saveHistory(FileTableEntity file, Long userId, String ua) {
        long time = System.currentTimeMillis();
        lock.lock();
        try {
            PlayRecordingEntity playRecordingEntity = findPlayRecordingEntityByArticleIdAndVideoId(file.getArticleId(), file.getId(), userId);
            if (playRecordingEntity != null) {
                playRecordingEntity.setUpdateTime(time);
                playRecordingEntity.setUa(ua);
                this.updateById(playRecordingEntity);
                return 0;
            } else {
                playRecordingEntity = new PlayRecordingEntity();
                playRecordingEntity.setArticleId(file.getArticleId());
                playRecordingEntity.setCreateTime(time);
                playRecordingEntity.setUpdateTime(time);
                playRecordingEntity.setUserId(userId);
                playRecordingEntity.setVideoId(file.getId());
                playRecordingEntity.setUa(ua);
                // TODO 采用缓存
                // articleService.addViewCount(file.getArticleId(), 1L);
                // TODO 播放时间戳，视频个数
                this.save(playRecordingEntity);
                return file.getArticleId();
            }
        } finally {
            lock.unlock();
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


//    public static void main(String[] args) {
//        System.out.println(System.currentTimeMillis());
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(simpleDateFormat.format(new Date(toDayZero())));
//    }
}