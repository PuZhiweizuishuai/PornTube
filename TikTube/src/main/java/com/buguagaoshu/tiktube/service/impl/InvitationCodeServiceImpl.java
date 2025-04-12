package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.exception.InvitationCodeException;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.InvitationCodeDao;
import com.buguagaoshu.tiktube.entity.InvitationCodeEntity;
import com.buguagaoshu.tiktube.service.InvitationCodeService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Pu Zhiwei
 * */
@Service("invitationCodeService")
public class InvitationCodeServiceImpl extends ServiceImpl<InvitationCodeDao, InvitationCodeEntity> implements InvitationCodeService {


    private final WebSettingCache webSettingCache;


    @Autowired
    public InvitationCodeServiceImpl(WebSettingCache webSettingCache) {
        this.webSettingCache = webSettingCache;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<InvitationCodeEntity> wrapper = new QueryWrapper<>();
        Integer type = (Integer) params.get("type");
        if (type != null) {
            if (type == 0) {
                wrapper.eq("use_user", 0);
            } else  {
                wrapper.eq("use_user", 1);
            }
        }
        wrapper.orderByDesc("create_time");
        IPage<InvitationCodeEntity> page = this.page(
                new Query<InvitationCodeEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public InvitationCodeEntity check(String invitationCode) {
        // 开启邀请码注册的情况下需要校验邀请码
        if (webSettingCache.getWebSettingEntity().getOpenInvitationRegister() == 1) {
            InvitationCodeEntity code
                    = this.getOne(new QueryWrapper<InvitationCodeEntity>().eq("code", invitationCode));
            if (code == null || code.getUseStatus() == 0) {
                throw new InvitationCodeException("邀请码错误！");
            }
            code.setUseTime(System.currentTimeMillis());
            code.setUseStatus(0);
            return code;
        }
        InvitationCodeEntity invitationCodeEntity = new InvitationCodeEntity();
        invitationCodeEntity.setId(-1L);
        return invitationCodeEntity;
    }

    @Override
    public ReturnCodeEnum create(HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        InvitationCodeEntity invitationCodeEntity = new InvitationCodeEntity();
        invitationCodeEntity.setUseStatus(1);
        invitationCodeEntity.setCreateTime(System.currentTimeMillis());
        invitationCodeEntity.setCode(UUID.randomUUID().toString().replace("-", ""));
        invitationCodeEntity.setCreateUser(userId);
        this.save(invitationCodeEntity);
        return ReturnCodeEnum.SUCCESS;
    }

}