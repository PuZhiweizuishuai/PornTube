package com.buguagaoshu.tiktube.utils;


import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:38
 */
@Slf4j
public class JwtUtil {

    public static String createJwt(String email, String userId, String role, long expirationTime, Long stopTime) {
        if (stopTime == null) {
            stopTime = -1L;
        }
        String jwt = Jwts.builder()
                // Subject 设置用户名
                .setSubject(email)
                .setId(userId)
                // 设置用户权限
                .claim(WebConstant.ROLE_KEY, role)
                .claim(WebConstant.VIP_STOP_TIME_KEY, stopTime)
                // 过期时间
                .setExpiration(new Date(expirationTime))
                // 签名算法
                .signWith(SignatureAlgorithm.HS512, WebConstant.SECRET_KEY)
                .compact();
        return jwt;
    }




    public static Claims getUser(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, WebConstant.COOKIE_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;

        if (token != null) {
            try {
                return Jwts.parser()
                        .setSigningKey(WebConstant.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                log.warn("来自 IP： {} 的用户 JWT TOKEN解析失败！", IpUtil.getIpAddr(request));
                return null;
            }

        }
        return null;
    }


    public static long getUserId(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute(WebConstant.USER_ID);
        if (userId == null) {
            Claims user = JwtUtil.getUser(request);
            if (user != null) {
                return Long.parseLong(user.getId());
            }
            throw new UserNotLoginException("当前用户未登录！");
        }
        return (long) userId;
    }

    public static String getRole(HttpServletRequest request) {
        String userRole = (String) request.getSession().getAttribute(WebConstant.ROLE_KEY);
        if (userRole == null) {
            Claims user = JwtUtil.getUser(request);
            if (user != null) {
                return (String)  user.get(WebConstant.ROLE_KEY);
            }
            throw new UserNotLoginException("当前用户未登录！");
        }
        return userRole;
    }
}
