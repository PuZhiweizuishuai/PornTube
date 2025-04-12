package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.entity.UserRoleEntity;
import com.buguagaoshu.tiktube.enums.RoleTypeEnum;
import com.buguagaoshu.tiktube.service.UserRoleService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-01 17:20
 */
@Service
public class LoginInterceptor implements HandlerInterceptor {
    private final UserRoleService userRoleService;

    @Autowired
    public LoginInterceptor(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Claims claims = JwtUtil.getUser(request);
        if (claims != null) {
            long userId = Long.parseLong(claims.getId());
            String role = (String) claims.get(WebConstant.ROLE_KEY);
            if (role.equals(RoleTypeEnum.VIP.getRole())) {
                long stopTime = (Long) claims.get(WebConstant.VIP_STOP_TIME_KEY);
                // VIP 到期
                if (stopTime < System.currentTimeMillis()) {
                    UserRoleEntity userRoleEntity = new UserRoleEntity();
                    userRoleEntity.setUserid(Long.parseLong(claims.getId()));
                    userRoleEntity.setRole(RoleTypeEnum.USER.getRole());
                    userRoleEntity.setUpdateTime(System.currentTimeMillis());
                    userRoleEntity.setModified(0L);
                    // 更新 权限数据
                    userRoleService.updateRoleByUserId(userRoleEntity);
                    // 更新 TOKEN
                    String jwt = JwtUtil.createJwt(claims.getSubject(), claims.getId(), RoleTypeEnum.USER.getRole(), claims.getExpiration().getTime(), -1L);
                    // 传递 token
                    Cookie cookie = WebUtils.getCookie(request, WebConstant.COOKIE_TOKEN);
                    if (cookie == null) {
                        return false;
                    }
                    cookie.setValue(jwt);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    return true;
                }
            }
            request.getSession().setAttribute(WebConstant.USER_ID, userId);
            request.getSession().setAttribute(WebConstant.ROLE_KEY, role);
            //request.setAttribute(WebConstant.USER_ID, userId);
            //request.setAttribute(WebConstant.ROLE_KEY, role);
            return true;
        }
        return false;
    }
}
