package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.dto.ArtDanmakuDto;
import com.buguagaoshu.tiktube.dto.DanmakuDto;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.DanmakuUtils;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;


import com.buguagaoshu.tiktube.dao.DanmakuDao;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.service.DanmakuService;

import jakarta.servlet.http.HttpServletRequest;

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
        long userId = -1;
        try {
            userId = JwtUtil.getUserId(request);
        }catch (UserNotLoginException e) {
            return ReturnCodeEnum.NO_LOGIN;
        }

        // ArticleEntity video = articleService.getById(danmakuDto.getId());
        FileTableEntity fileTableEntity = fileTableService.getById(danmakuDto.getId());
        if (fileTableEntity == null && fileTableEntity.getArticleId() != null) {
            return ReturnCodeEnum.NOO_FOUND;
        }

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

    /**
     * {
     *     text: '', // 弹幕文本
     *     time: 10, // 弹幕时间, 默认为当前播放器时间
     *     mode: 0, // 弹幕模式: 0: 滚动(默认)，1: 顶部，2: 底部
     *     color: '#FFFFFF', // 弹幕颜色，默认为白色
     *     border: false, // 弹幕是否有描边, 默认为 false
     *     style: {}, // 弹幕自定义样式, 默认为空对象
     * }
     * 区别在于 dplayer 的 mode 为 type
     */
    @Override
    public ReturnCodeEnum saveArtDanmaku(ArtDanmakuDto danmakuDto, HttpServletRequest request) {
        long userId = -1;
        try {
            userId = JwtUtil.getUserId(request);
        }catch (UserNotLoginException e) {
            return ReturnCodeEnum.NO_LOGIN;
        }

        // ArticleEntity video = articleService.getById(danmakuDto.getId());
        FileTableEntity fileTableEntity = fileTableService.getById(danmakuDto.getId());
        if (fileTableEntity == null && fileTableEntity.getArticleId() != null) {
            return ReturnCodeEnum.NOO_FOUND;
        }

        DanmakuEntity danmakuEntity = new DanmakuEntity();
        danmakuEntity.setAuthor(userId);

        danmakuEntity.setVideoId(danmakuDto.getId());
        danmakuEntity.setText(danmakuDto.getText());
        danmakuEntity.setColor(danmakuDto.getColor());
        danmakuEntity.setTime(danmakuDto.getTime());
        danmakuEntity.setType(danmakuDto.getType());
        // TODO 升级完成后这部分可以删除
        // 去掉开头的 # 符号
        String cleanHex = danmakuDto.getColor().replace("#", "");
        // 将十六进制字符串转换为十进制整数
        danmakuEntity.setColorDec(Long.parseLong(cleanHex, 16));
        this.save(danmakuEntity);
        // TODO 加入缓存，提升效率
        articleService.addDanmakuCount(fileTableEntity.getArticleId(), 1L);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public List<DanmakuEntity> artDanmakuList(Long id, Integer max) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("limit", max);
        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                new QueryWrapper<DanmakuEntity>().eq("video_id", id)
        );
        return page.getRecords();
    }

}