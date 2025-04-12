package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.WebSettingEntity;

import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
public interface WebSettingService extends IService<WebSettingEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取最新的web设置
     *
     * @return 设置
     */
    WebSettingEntity getNewSetting();


    ReturnCodeEnum saveSetting(WebSettingEntity webSettingEntity);
}

