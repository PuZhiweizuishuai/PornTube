package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.dao.FavoritesLabelDao;
import com.buguagaoshu.tiktube.dao.FavoritesTableDao;
import com.buguagaoshu.tiktube.entity.FavoritesLabelEntity;
import com.buguagaoshu.tiktube.entity.FavoritesTableEntity;
import com.buguagaoshu.tiktube.service.FavoritesLabelService;
import com.buguagaoshu.tiktube.vo.FavoritesLabelWithCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("favoritesLabelService")
public class FavoritesLabelServiceImpl extends ServiceImpl<FavoritesLabelDao, FavoritesLabelEntity> implements FavoritesLabelService {

    final FavoritesTableDao favoritesTableDao;

    @Autowired
    public FavoritesLabelServiceImpl(FavoritesTableDao favoritesTableDao) {
        this.favoritesTableDao = favoritesTableDao;
    }

    @Override
    public FavoritesLabelEntity getOrCreateDefaultLabel(Long userId) {
        QueryWrapper<FavoritesLabelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_default", 1);
        wrapper.orderByAsc("id");
        List<FavoritesLabelEntity> list = this.list(wrapper);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        FavoritesLabelEntity defaultLabel = new FavoritesLabelEntity();
        defaultLabel.setName("默认收藏夹");
        defaultLabel.setUserId(userId);
        defaultLabel.setIsPublic(0);
        defaultLabel.setIsDefault(1);
        defaultLabel.setCreateTime(System.currentTimeMillis());
        this.save(defaultLabel);
        return defaultLabel;
    }

    @Override
    public List<FavoritesLabelWithCount> listWithCount(Long userId) {
        QueryWrapper<FavoritesLabelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("id");
        return wrapWithCount(this.list(wrapper));
    }

    @Override
    public List<FavoritesLabelWithCount> publicLabelList(Long userId) {
        QueryWrapper<FavoritesLabelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_public", 1);
        wrapper.orderByAsc("id");
        return wrapWithCount(this.list(wrapper));
    }

    @Override
    public boolean existsName(Long userId, String name, Long excludeId) {
        QueryWrapper<FavoritesLabelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("name", name);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLabel(Long userId, Long labelId) {
        FavoritesLabelEntity label = this.getById(labelId);
        if (label == null || !label.getUserId().equals(userId)) {
            return false;
        }
        if (Integer.valueOf(1).equals(label.getIsDefault())) {
            throw new IllegalArgumentException("默认收藏夹不能删除");
        }
        FavoritesLabelEntity defaultLabel = getOrCreateDefaultLabel(userId);
        UpdateWrapper<FavoritesTableEntity> move = new UpdateWrapper<>();
        move.eq("user_id", userId);
        move.eq("favorites_label_id", labelId);
        move.set("favorites_label_id", defaultLabel.getId());
        favoritesTableDao.update(null, move);
        return this.removeById(labelId);
    }

    private List<FavoritesLabelWithCount> wrapWithCount(List<FavoritesLabelEntity> labels) {
        Map<Long, Long> countMap = favoriteCountByLabel(labels);
        List<FavoritesLabelWithCount> result = new ArrayList<>(labels.size());
        for (FavoritesLabelEntity label : labels) {
            FavoritesLabelWithCount vo = new FavoritesLabelWithCount();
            vo.setId(label.getId());
            vo.setName(label.getName());
            vo.setUserId(label.getUserId());
            vo.setIsPublic(label.getIsPublic());
            vo.setIsDefault(label.getIsDefault());
            vo.setCreateTime(label.getCreateTime());
            vo.setCount(countMap.getOrDefault(label.getId(), 0L));
            result.add(vo);
        }
        return result;
    }

    private Map<Long, Long> favoriteCountByLabel(List<FavoritesLabelEntity> labels) {
        Map<Long, Long> countMap = new HashMap<>();
        if (labels.isEmpty()) {
            return countMap;
        }
        QueryWrapper<FavoritesTableEntity> wrapper = new QueryWrapper<>();
        wrapper.select("favorites_label_id", "count(1) as cnt");
        wrapper.in("favorites_label_id", labels.stream().map(FavoritesLabelEntity::getId).toList());
        wrapper.groupBy("favorites_label_id");
        for (Map<String, Object> row : favoritesTableDao.selectMaps(wrapper)) {
            Object labelId = row.get("favorites_label_id");
            Object cnt = row.get("cnt");
            if (labelId != null && cnt != null) {
                countMap.put(((Number) labelId).longValue(), ((Number) cnt).longValue());
            }
        }
        return countMap;
    }
}
