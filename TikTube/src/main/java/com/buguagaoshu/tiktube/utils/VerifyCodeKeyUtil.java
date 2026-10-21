package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.config.WebConstant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;

import java.util.UUID;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-21
 * 图形验证码的键。
 * <p>
 * 键必须跟着浏览器走，不能跟着 HttpSession 走：会话是每个实例自己的，用 session.getId() 当键时，
 * 「取图片」和「提交表单」落到不同实例就会验不过，即使验证码本身存在 Redis 里也一样。
 * 这里改成服务端下发的随机不透明 ID，用 HttpOnly Cookie 回传，前端不需要任何改动。
 */
public final class VerifyCodeKeyUtil {

    /**
     * 键的存活时间，需要覆盖邮箱验证码 15 分钟的窗口
     */
    private static final int COOKIE_MAX_AGE_SECONDS = 1800;

    private VerifyCodeKeyUtil() {
    }

    /**
     * 下发一个新的键并写进 Cookie，返回值即服务端存取验证码用的键
     */
    public static String issue(HttpServletResponse response) {
        String key = UUID.randomUUID().toString().replace("-", "");
        Cookie cookie = new Cookie(WebConstant.COOKIE_VERIFY_KEY, key);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
        return key;
    }

    /**
     * 读出浏览器带回的键，没有则返回 null，由调用方提示刷新验证码
     */
    public static String read(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, WebConstant.COOKIE_VERIFY_KEY);
        if (cookie == null) {
            return null;
        }
        String value = cookie.getValue();
        return value == null || value.isEmpty() ? null : value;
    }
}
