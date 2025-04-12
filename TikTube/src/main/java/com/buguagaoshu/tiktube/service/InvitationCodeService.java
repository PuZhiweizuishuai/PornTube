package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.InvitationCodeEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface InvitationCodeService extends IService<InvitationCodeEntity> {

    /**
     * 邀请码列表
     * */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 邀请码校验
     * @param invitationCode 邀请码
     * */
    InvitationCodeEntity check(String invitationCode);

    /**
     * 创建邀请码
     * */
    ReturnCodeEnum create(HttpServletRequest request);
}

