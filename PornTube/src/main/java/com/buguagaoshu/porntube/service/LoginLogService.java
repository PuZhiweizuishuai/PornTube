package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.utils.PageUtils;

import com.buguagaoshu.porntube.entity.LoginLogEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
public interface LoginLogService extends IService<LoginLogEntity> {

    /**
     * 查找登录历史
     * */
    PageUtils queryPage(HttpServletRequest request, Map<String, Object> params);


    /**
     * 保存登录日志
     * */
    void saveLoginLog(UserEntity userEntity, HttpServletRequest request);
}

