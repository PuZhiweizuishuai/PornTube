package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 播放记录
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface PlayRecordingDao extends BaseMapper<PlayRecordingEntity> {

    /**
     * 批量写入播放记录，依赖 (user_id, article_id, video_id) 唯一索引做幂等 upsert：
     * 已存在则推进观看时间与进度，不存在则插入
     */
    void batchUpsert(@Param("list") List<PlayRecordingEntity> list);
}
