package com.buguagaoshu.tiktube.support;

import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户解析器
 *
 * 收敛 <code>try { JwtUtil.getUserId(request) } catch (UserNotLoginException) { -1 }</code>
 * 这一惯用法（原实现散落在多个 service 中，且异常被静默吞掉）。
 *
 * 约定：
 *  - 未登录 → {@link CurrentUser#anonymous()}，userId = -1
 *  - 需要「必须登录」的场景仍然直接调用 JwtUtil.getUserId(request)，让它抛出未登录异常
 */
@Slf4j
@Component
public class CurrentUserResolver {

    /**
     * 解析当前请求的用户信息，未登录返回匿名用户。
     * userId 与 role 一次性解析完成，避免重复读取 JWT。
     */
    public CurrentUser resolve(HttpServletRequest request) {
        if (request == null) {
            return CurrentUser.anonymous();
        }
        long userId = CurrentUser.ANONYMOUS_ID;
        String role = null;
        try {
            userId = JwtUtil.getUserId(request);
        } catch (UserNotLoginException ignored) {
            return CurrentUser.anonymous();
        } catch (RuntimeException e) {
            // JWT 损坏等异常不应影响匿名浏览
            log.warn("解析当前用户 ID 失败，按未登录处理: {}", e.getMessage());
            return CurrentUser.anonymous();
        }
        try {
            role = JwtUtil.getRole(request);
        } catch (RuntimeException e) {
            log.warn("解析当前用户角色失败，按普通用户处理: {}", e.getMessage());
        }
        return CurrentUser.of(userId, role);
    }

    /** 当前用户 ID，未登录返回 -1 */
    public long currentUserIdOrAnonymous(HttpServletRequest request) {
        return resolve(request).getUserId();
    }

    /** 当前用户 ID，未登录返回 null */
    public Long currentUserIdOrNull(HttpServletRequest request) {
        return resolve(request).getUserIdOrNull();
    }

    public boolean isLogin(HttpServletRequest request) {
        return resolve(request).isLogin();
    }

    /** 是否为管理员（未登录返回 false，不再抛异常） */
    public boolean isAdmin(HttpServletRequest request) {
        return resolve(request).isAdmin();
    }

    /** 当前角色，未登录返回 null */
    public String currentRoleOrNull(HttpServletRequest request) {
        return resolve(request).getRole();
    }

    /** 是否为资源所有者或管理员 */
    public boolean isOwnerOrAdmin(Long ownerId, HttpServletRequest request) {
        return resolve(request).isOwnerOrAdmin(ownerId);
    }
}
