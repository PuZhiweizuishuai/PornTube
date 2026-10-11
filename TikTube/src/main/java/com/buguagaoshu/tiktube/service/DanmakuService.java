package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.buguagaoshu.tiktube.dto.ArtDanmakuDto;
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


    ReturnCodeEnum saveArtDanmaku(ArtDanmakuDto danmakuDto, HttpServletRequest request);


    /**
     * 获取所有弹幕列表（管理员接口）
     */
    PageUtils getAllDanmaku(Map<String, Object> params);

    /**
     * 切换弹幕状态（管理员接口）
     */
    boolean toggleDanmakuStatus(long id);


    List<DanmakuEntity> artDanmakuList(Long id, Integer max);
}

