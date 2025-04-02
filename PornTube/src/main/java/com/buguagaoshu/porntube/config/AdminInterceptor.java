package com.buguagaoshu.porntube.config;

import com.buguagaoshu.porntube.enums.RoleTypeEnum;
import com.buguagaoshu.porntube.utils.IpUtil;
import com.buguagaoshu.porntube.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 17:05
 */
@Service
@Slf4j
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Claims claims = JwtUtil.getUser(request);
        if (claims == null) {
            log.warn("访问 ip 为 {} 访问管理员接口被拦截", IpUtil.getIpAddr(request));
            return false;
        }
        if (RoleTypeEnum.ADMIN.getRole().equals(claims.get("authorities"))) {
            return true;
        }
        log.warn("用户id为 {} 的用户访问管理员接口被拦截，访问 ip 为 {}", claims.getId(), IpUtil.getIpAddr(request));
        return false;
    }
}
