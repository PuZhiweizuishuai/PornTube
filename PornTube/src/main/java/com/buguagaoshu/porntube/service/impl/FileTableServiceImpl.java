package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.enums.FileTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.FileTableDao;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.service.FileTableService;


@Service("fileTableService")
public class FileTableServiceImpl extends ServiceImpl<FileTableDao, FileTableEntity> implements FileTableService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<FileTableEntity> page = this.page(
                new Query<FileTableEntity>().getPage(params),
                new QueryWrapper<FileTableEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<FileTableEntity> findArticleVideo(long id) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", id);
        wrapper.eq("type", FileTypeEnum.VIDEO.getCode());
        return this.list(wrapper);
    }

}