package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.dto.ArtDanmakuDto;
import com.buguagaoshu.tiktube.dto.DanmakuDto;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 弹幕表
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-07 16:00:19
 */
public interface DanmakuService extends IService<DanmakuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取弹幕列表
     * 重构前端后使用新版播放器，新版播放器弹幕与旧版有区别
     * */
    @Deprecated
    List<Object> danmakuList(Long id, Integer max);

    /**
     * 保存弹幕
     * */
    @Deprecated
    ReturnCodeEnum saveDanmaku(DanmakuDto danmakuDto, HttpServletRequest request);


    ReturnCodeEnum saveArtDanmaku(ArtDanmakuDto danmakuDto, HttpServletRequest request);


    List<DanmakuEntity> artDanmakuList(Long id, Integer max);
}

