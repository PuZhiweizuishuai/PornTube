package com.buguagaoshu.porntube.utils;

import com.buguagaoshu.porntube.service.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:38
 */
public class JwtUtil {
    public final static String SECRET_KEY = "adfads@44$q232343#";


    public static String createJwt(String email, String userId, String role, long expirationTime) {
        String jwt = Jwts.builder()
                // Subject 设置用户名
                .setSubject(email)
                .setId(userId)
                // 设置用户权限
                .claim("authorities", role)
                // 过期时间
                .setExpiration(new Date(expirationTime))
                // 签名算法
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
        return jwt;
    }

    public static Claims getUser(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, UserServiceImpl.COOKIE_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;

        if (token != null) {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        }
        return null;
    }
}
