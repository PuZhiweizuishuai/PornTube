package com.buguagaoshu.porntube.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;
import com.buguagaoshu.porntube.dao.PlayRecordingDao;
import com.buguagaoshu.porntube.entity.PlayRecordingEntity;
import com.buguagaoshu.porntube.service.PlayRecordingService;


@Service("playRecordingService")
public class PlayRecordingServiceImpl extends ServiceImpl<PlayRecordingDao, PlayRecordingEntity> implements PlayRecordingService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PlayRecordingEntity> page = this.page(
                new Query<PlayRecordingEntity>().getPage(params),
                new QueryWrapper<PlayRecordingEntity>()
        );

        return new PageUtils(page);
    }

}