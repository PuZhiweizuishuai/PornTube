package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.dto.DanmakuDto;
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.utils.DanmakuUtils;
import com.buguagaoshu.porntube.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;


import com.buguagaoshu.porntube.dao.DanmakuDao;
import com.buguagaoshu.porntube.entity.DanmakuEntity;
import com.buguagaoshu.porntube.service.DanmakuService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pu Zhiwei
 * */
@Service("danmakuService")
public class DanmakuServiceImpl extends ServiceImpl<DanmakuDao, DanmakuEntity> implements DanmakuService {

    private final ArticleService articleService;

    private final FileTableService fileTableService;

    @Autowired
    public DanmakuServiceImpl(ArticleService articleService, FileTableService fileTableService) {
        this.articleService = articleService;
        this.fileTableService = fileTableService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                new QueryWrapper<DanmakuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<Object> danmakuList(Long id, Integer max) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("limit", max);
        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                new QueryWrapper<DanmakuEntity>().eq("video_id", id)
        );
        List<DanmakuEntity> danmakuEntities = page.getRecords();

       List<Object> danmakuDtos = new LinkedList<>();
       danmakuEntities.forEach(d->{
           danmakuDtos.add(DanmakuUtils.createDanmaku(
                   d.getTime(),
                   d.getType(),
                   d.getColorDec(),
                   d.getColor(),
                   d.getText()
           ));

       });
       return danmakuDtos;
    }

    @Override
    public ReturnCodeEnum saveDanmaku(DanmakuDto danmakuDto, HttpServletRequest request) {
        Claims claims = JwtUtil.getUser(request);
        if (claims == null) {
            return ReturnCodeEnum.NO_LOGIN;
        }
        // ArticleEntity video = articleService.getById(danmakuDto.getId());
        FileTableEntity fileTableEntity = fileTableService.getById(danmakuDto.getId());
        if (fileTableEntity == null && fileTableEntity.getArticleId() != null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        Long userId = Long.parseLong(claims.getId());
        DanmakuEntity danmakuEntity = new DanmakuEntity();
        danmakuEntity.setAuthor(userId);

        danmakuEntity.setColorDec(danmakuDto.getColor());
        danmakuEntity.setVideoId(danmakuDto.getId());
        danmakuEntity.setText(danmakuDto.getText());
        danmakuEntity.setColor(Long.toHexString(danmakuDto.getColor()));
        danmakuEntity.setTime(danmakuDto.getTime());
        danmakuEntity.setType(danmakuDto.getType());
        this.save(danmakuEntity);
        // TODO 加入缓存，提升效率
        articleService.addDanmakuCount(fileTableEntity.getArticleId(), 1L);
        return ReturnCodeEnum.SUCCESS;
    }

}