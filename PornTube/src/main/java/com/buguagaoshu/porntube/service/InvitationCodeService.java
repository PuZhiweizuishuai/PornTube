package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.InvitationCodeEntity;

import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface InvitationCodeService extends IService<InvitationCodeEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 邀请码校验
     * @param invitationCode 邀请码
     * */
    InvitationCodeEntity check(String invitationCode);
}

