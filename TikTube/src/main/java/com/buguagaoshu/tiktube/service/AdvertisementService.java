package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.cache.AdsCountRecorder;
import com.buguagaoshu.tiktube.cache.AdvertisementCache;
import com.buguagaoshu.tiktube.cache.SharedCache;
import com.buguagaoshu.tiktube.cache.SharedCacheVersionStore;
import com.buguagaoshu.tiktube.dao.AdvertisementDao;
import com.buguagaoshu.tiktube.entity.AdvertisementEntity;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-03
 */
@Service
@Slf4j
public class AdvertisementService extends ServiceImpl<AdvertisementDao, AdvertisementEntity> {

    private final AdsCountRecorder adsCountRecorder;

    private final SharedCacheVersionStore versionStore;

    @Autowired
    public AdvertisementService(AdsCountRecorder adsCountRecorder,
                               SharedCacheVersionStore versionStore) {
        this.adsCountRecorder = adsCountRecorder;
        this.versionStore = versionStore;
    }

    public AdvertisementEntity save(AdvertisementEntity advertisement, long userId) {
        long time = System.currentTimeMillis();
        advertisement.setCreateTime(time);
        advertisement.setUpdateTime(time);
        advertisement.setCreateUser(userId);
        advertisement.setViewCount(0L);
        this.save(advertisement);
        getNowAdvertisement(true);
        return advertisement;
    }

    public AdvertisementEntity update(AdvertisementEntity advertisement, long userId) {
        advertisement.setUpdateTime(System.currentTimeMillis());
        advertisement.setViewCount(null);
        this.updateById(advertisement);
        getNowAdvertisement(true);
        return advertisement;
    }


    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<AdvertisementEntity> wrapper = new QueryWrapper<>();
        String type = (String) params.get("type");
        String status = (String) params.get("status");
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("update_time");
        IPage<AdvertisementEntity> page = this.page(
                new Query<AdvertisementEntity>().getPage(params),
                wrapper
        );
        // 同步访问量
        page.getRecords().forEach(advertisementEntity -> {
            advertisementEntity.setViewCount(advertisementEntity.getViewCount() + adsCountRecorder.getAdsCount(advertisementEntity.getId()));
        });
        return new PageUtils(page);
    }

    /**
     * 由 CountTasks 在独立事务中调用，写入已 drain 出来的广告浏览增量快照
     */
    public void updateViewCounts(Map<Integer, Long> adsCountSnapshot) {
        this.baseMapper.batchUpdateCount("view_count", adsCountSnapshot);
    }


    /**
     * 获取当前有效的广告
     * 查询条件：当前时间在广告的开始时间和结束时间之间
     *
     * @param broadcast 是否把新版本广播给其他实例，定时任务传 false，后台增删改和手动同步传 true
     */
    public void getNowAdvertisement(boolean broadcast) {
        long time = System.currentTimeMillis();
        QueryWrapper<AdvertisementEntity> wrapper = new QueryWrapper<>();
        // 查询条件：startTime < 当前时间 < endTime 且状态为启用(1)
        wrapper.lt("start_time", time)
               .gt("end_time", time)
               .eq("status", 1);
        wrapper.orderByDesc("update_time");
        List<AdvertisementEntity> list = this.list(wrapper);
        Map<Integer, List<AdvertisementEntity>> advertisementCache = new HashMap<Integer, List<AdvertisementEntity>>();
        if (list != null) {
            // 按照 type 值进行归类
            for (AdvertisementEntity advertisement : list) {
                Integer type = advertisement.getType();
                if (!advertisementCache.containsKey(type)) {
                    advertisementCache.put(type, new ArrayList<>());
                }
                advertisementCache.get(type).add(advertisement);
            }
        }
        // 空结果也要覆盖：广告全部过期或删除后，不能继续展示上一份缓存
        AdvertisementCache.advertisementCache = advertisementCache;
        if (broadcast) {
            versionStore.markUpdated(SharedCache.ADVERTISEMENT);
        }
    }

}
