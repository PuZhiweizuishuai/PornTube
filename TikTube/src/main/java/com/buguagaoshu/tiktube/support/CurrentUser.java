package com.buguagaoshu.tiktube.support;

import com.buguagaoshu.tiktube.enums.RoleTypeEnum;

/**
 * 当前请求用户的一次性解析结果
 *
 * 背景：全站到处写着
 * <pre>
 *   long userId = -1;
 *   try { userId = JwtUtil.getUserId(request); } catch (UserNotLoginException ignored) {}
 *   boolean isAdmin = false;
 *   try { if (ADMIN.equals(JwtUtil.getRole(request))) isAdmin = true; } catch (Exception e) {}
 * </pre>
 * 同一段逻辑在 ArticleServiceImpl / ArticleTextService / DanmakuServiceImpl 各写一遍，
 * 而且 getUserId / getRole 会被调用两次（重复解析 JWT）。
 * 这里把结果收敛成一个不可变对象，由 {@link CurrentUserResolver} 一次解析、多处复用。
 */
public final class CurrentUser {

    /** 未登录用户的约定 ID，与项目中原有语义保持一致 */
    public static final long ANONYMOUS_ID = -1L;

    private static final CurrentUser ANONYMOUS = new CurrentUser(ANONYMOUS_ID, null);

    private final long userId;

    private final String role;

    private CurrentUser(long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public static CurrentUser anonymous() {
        return ANONYMOUS;
    }

    public static CurrentUser of(long userId, String role) {
        if (userId == ANONYMOUS_ID) {
            return ANONYMOUS;
        }
        return new CurrentUser(userId, role);
    }

    public long getUserId() {
        return userId;
    }

    /** 未登录时返回 null，便于直接写入可为 null 的字段 */
    public Long getUserIdOrNull() {
        return isLogin() ? userId : null;
    }

    public String getRole() {
        return role;
    }

    public boolean isLogin() {
        return userId != ANONYMOUS_ID;
    }

    public boolean isAdmin() {
        return isLogin() && RoleTypeEnum.ADMIN.getRole().equals(role);
    }

    /** 是否为该资源的所有者（未登录恒为 false） */
    public boolean isOwner(Long ownerId) {
        return isLogin() && ownerId != null && ownerId.longValue() == userId;
    }

    /** 是否为该资源的所有者或管理员 */
    public boolean isOwnerOrAdmin(Long ownerId) {
        return isAdmin() || isOwner(ownerId);
    }
}
