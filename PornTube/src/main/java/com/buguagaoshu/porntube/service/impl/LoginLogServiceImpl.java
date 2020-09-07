package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.utils.IpUtil;
import org.apache.tomcat.util.net.IPv6Utils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.LoginLogDao;
import com.buguagaoshu.porntube.entity.LoginLogEntity;
import com.buguagaoshu.porntube.service.LoginLogService;

import javax.servlet.http.HttpServletRequest;


@Service("loginLogService")
public class LoginLogServiceImpl extends ServiceImpl<LoginLogDao, LoginLogEntity> implements LoginLogService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<LoginLogEntity> page = this.page(
                new Query<LoginLogEntity>().getPage(params),
                new QueryWrapper<LoginLogEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveLoginLog(UserEntity userEntity, HttpServletRequest request) {
        LoginLogEntity loginLogEntity = new LoginLogEntity();
        loginLogEntity.setUserid(userEntity.getId());
        loginLogEntity.setIp(IpUtil.getIpAddr(request));
        loginLogEntity.setLoginTime(System.currentTimeMillis());
        loginLogEntity.setUa(request.getHeader("user-agent"));
        save(loginLogEntity);
    }

}