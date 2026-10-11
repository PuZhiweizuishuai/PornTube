package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.dto.ArtDanmakuDto;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.enums.TypeCode;
import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import com.buguagaoshu.tiktube.pipe.AiExaminePipe;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;


import com.buguagaoshu.tiktube.dao.DanmakuDao;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.service.DanmakuService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pu Zhiwei
 * */
@Service("danmakuService")
public class DanmakuServiceImpl extends ServiceImpl<DanmakuDao, DanmakuEntity> implements DanmakuService {

    private final ArticleService articleService;

    private final FileTableService fileTableService;

    private final CountRecorder countRecorder;

    private final WebSettingCache webSettingCache;

    private final IpUtil ipUtil;

    private final AiExaminePipe aiExaminePipe;

    @Autowired
    public DanmakuServiceImpl(ArticleService articleService,
                              FileTableService fileTableService,
                              CountRecorder countRecorder,
                              WebSettingCache webSettingCache,
                              IpUtil ipUtil, AiExaminePipe aiExaminePipe) {
        this.articleService = articleService;
        this.fileTableService = fileTableService;
        this.countRecorder = countRecorder;
        this.webSettingCache = webSettingCache;
        this.ipUtil = ipUtil;
        this.aiExaminePipe = aiExaminePipe;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                new QueryWrapper<DanmakuEntity>()
        );

        return new PageUtils(page);
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
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveArtDanmaku(ArtDanmakuDto danmakuDto, HttpServletRequest request) {
        long userId = -1;
        try {
            userId = JwtUtil.getUserId(request);
        }catch (UserNotLoginException e) {
            return ReturnCodeEnum.NO_LOGIN;
        }

        // ArticleEntity video = articleService.getById(danmakuDto.getId());
        FileTableEntity fileTableEntity = fileTableService.getById(danmakuDto.getId());
        if (fileTableEntity == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        if (fileTableEntity.getArticleId() == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }

        DanmakuEntity danmakuEntity = new DanmakuEntity();
        danmakuEntity.setAuthor(userId);
        danmakuEntity.setCreateTime(System.currentTimeMillis());
        danmakuEntity.setVideoId(danmakuDto.getId());
        danmakuEntity.setText(danmakuDto.getText());
        danmakuEntity.setColor(danmakuDto.getColor());
        danmakuEntity.setTime(danmakuDto.getTime());
        danmakuEntity.setType(danmakuDto.getType());
        String ip = ipUtil.getIpAddr(request);
        danmakuEntity.setIp(ip);
        danmakuEntity.setUa(ipUtil.getUa(request));
        danmakuEntity.setCity(ipUtil.getCity(ip));
        // TODO 升级完成后这部分可以删除
        // 去掉开头的 # 符号
        String cleanHex = danmakuDto.getColor().replace("#", "");
        // 将十六进制字符串转换为十进制整数
        danmakuEntity.setColorDec(Long.parseLong(cleanHex, 16));
        // 如果开启弹幕审核
        if (webSettingCache.getWebConfigData().getOpenDanmakuExam()) {
            danmakuEntity.setStatus(TypeCode.EXAM);
            this.save(danmakuEntity);
            if (webSettingCache.getWebConfigData().getOpenAIConfig()) {
                aiExaminePipe.submitDanmaku(danmakuEntity);
            }
            countRecorder.recordDanmaku(fileTableEntity.getArticleId(), 1L);
            return ReturnCodeEnum.NEED_EXAM;
        } else {
            danmakuEntity.setStatus(TypeCode.NORMAL);
            this.save(danmakuEntity);
            countRecorder.recordDanmaku(fileTableEntity.getArticleId(), 1L);
            return ReturnCodeEnum.SUCCESS;
        }
    }

    @Override
    public PageUtils getAllDanmaku(Map<String, Object> params) {
        QueryWrapper<DanmakuEntity> wrapper = new QueryWrapper<>();
        String userId = (String) params.get("userId");
        String videoId = (String) params.get("videoId");
        String status = (String) params.get("status");

        if (userId != null) {
            wrapper.eq("author", userId);
        }
        if (videoId != null) {
            wrapper.eq("video_id", videoId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleDanmakuStatus(long id) {
        DanmakuEntity danmaku = this.getById(id);
        if (danmaku == null) {
            return false;
        }
        FileTableEntity fileTableEntity = fileTableService.getById(danmaku.getVideoId());
        // 切换状态：正常 <-> 删除
        if (danmaku.getStatus().equals(ArticleStatusEnum.DELETE.getCode())) {
            danmaku.setStatus(ArticleStatusEnum.NORMAL.getCode());
            articleService.addDanmakuCount(fileTableEntity.getArticleId(), 1L);
        } else {
            danmaku.setStatus(ArticleStatusEnum.DELETE.getCode());
            articleService.addDanmakuCount(fileTableEntity.getArticleId(), -1L);
        }

        this.updateById(danmaku);
        return true;
    }

    @Override
    public List<DanmakuEntity> artDanmakuList(Long id, Integer max) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("limit", max);
        QueryWrapper<DanmakuEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", id);
        wrapper.eq("status", TypeCode.NORMAL);
        wrapper.orderByDesc("create_time");
        IPage<DanmakuEntity> page = this.page(
                new Query<DanmakuEntity>().getPage(params),
                wrapper
        );
        page.getRecords().parallelStream().forEach(d -> {
            d.setCity(null);
            d.setIp(null);
            d.setUa(null);
        });
        return page.getRecords();
    }

}