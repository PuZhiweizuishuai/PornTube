package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.LoginLogDao;
import com.buguagaoshu.tiktube.entity.LoginLogEntity;
import com.buguagaoshu.tiktube.service.LoginLogService;

import jakarta.servlet.http.HttpServletRequest;


@Service("loginLogService")
public class LoginLogServiceImpl extends ServiceImpl<LoginLogDao, LoginLogEntity> implements LoginLogService {

    @Override
    public PageUtils queryPage(HttpServletRequest request, Map<String, Object> params) {
        long userId = JwtUtil.getUserId(request);

        QueryWrapper<LoginLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        wrapper.orderByDesc("login_time");
        IPage<LoginLogEntity> page = this.page(
                new Query<LoginLogEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void saveLoginLog(UserEntity userEntity, HttpServletRequest request) {
        LoginLogEntity loginLogEntity = new LoginLogEntity();
        loginLogEntity.setUserid(userEntity.getId());
        loginLogEntity.setIp(IpUtil.getIpAddr(request));
        loginLogEntity.setLoginTime(System.currentTimeMillis());
        loginLogEntity.setUa(IpUtil.getUa(request));
        save(loginLogEntity);
    }

}