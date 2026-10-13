package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.enums.RoleTypeEnum;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投稿权限拦截器
 * <p>
 * 是否放行普通用户投稿由「系统设置 - 投稿权限设置」决定（{@link WebConfigData}），
 * 不再需要通过注释/取消注释 {@link WebConfig} 里的拦截配置来切换演示环境与生产环境。
 * VIP 与管理员始终放行。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-20
 */
@Service
@Slf4j
public class VipInterceptor implements HandlerInterceptor {

    /**
     * 受投稿权限设置控制的接口
     */
    private enum PostApi {
        /**
         * 发布视频、上传视频文件、上传视频封面
         */
        VIDEO("当前系统设置未开放普通用户投稿视频！",
                List.of("/api/article/video", "/api/upload/video", "/api/upload/photo")),
        /**
         * 发布文章、上传文章正文素材与文章封面
         */
        ARTICLE("当前系统设置未开放普通用户投稿文章！",
                List.of("/api/article/text", "/api/upload/article", "/api/upload/article/cover")),
        /**
         * 修改、删除已投稿的内容（视频与文章共用这些接口），只跟随总开关
         */
        MANAGE("当前系统设置未开放普通用户修改和删除投稿内容！",
                List.of("/api/article/video/update", "/api/article/text/update", "/api/studio/article/delete"));

        private final String message;
        private final List<String> paths;

        PostApi(String message, List<String> paths) {
            this.message = message;
            this.paths = paths;
        }

        /**
         * 该接口是否对普通用户开放
         */
        boolean openTo(WebConfigData webConfigData) {
            if (!Boolean.TRUE.equals(webConfigData.getOpenNormalUserPost())) {
                return false;
            }
            return switch (this) {
                case VIDEO -> Boolean.TRUE.equals(webConfigData.getOpenNormalUserPostVideo());
                case ARTICLE -> Boolean.TRUE.equals(webConfigData.getOpenNormalUserPostArticle());
                // 总开关打开即允许管理自己已发布的内容，不受分类开关限制
                case MANAGE -> true;
            };
        }
    }

    /**
     * 拦截器需要注册的全部接口
     */
    public static String[] postApiPaths() {
        List<String> paths = new ArrayList<>();
        for (PostApi postApi : PostApi.values()) {
            paths.addAll(postApi.paths);
        }
        return paths.toArray(new String[0]);
    }

    private static PostApi match(String servletPath) {
        for (PostApi postApi : PostApi.values()) {
            if (postApi.paths.contains(servletPath)) {
                return postApi;
            }
        }
        return null;
    }

    private final IpUtil ipUtil;

    private final WebSettingCache webSettingCache;

    @Autowired
    public VipInterceptor(IpUtil ipUtil, WebSettingCache webSettingCache) {
        this.ipUtil = ipUtil;
        this.webSettingCache = webSettingCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Claims claims = JwtUtil.getUser(request);
        String servletPath = request.getServletPath();
        if (claims == null) {
            log.warn("访问 ip 为 {} 未登录访问 {} 接口被拦截", ipUtil.getIpAddr(request), servletPath);
            unauthorized(response);
            return false;
        }
        String role = (String) claims.get(WebConstant.ROLE_KEY);
        if (RoleTypeEnum.VIP.getRole().equals(role) || RoleTypeEnum.ADMIN.getRole().equals(role)) {
            return true;
        }
        PostApi postApi = match(servletPath);
        WebConfigData webConfigData = webSettingCache.getWebConfigData();
        if (postApi == null || (webConfigData != null && postApi.openTo(webConfigData))) {
            return true;
        }
        log.warn("用户id为 {} 的用户访问 {} 接口被拦截，访问 ip 为 {}", claims.getId(), servletPath, ipUtil.getIpAddr(request));
        reject(response, postApi.message);
        return false;
    }

    /**
     * 以业务状态码返回，前端各投稿表单已有的 json.status / json.message 处理逻辑能直接展示提示
     */
    private void reject(HttpServletResponse response, String message) throws Exception {
        write(response, HttpServletResponse.SC_OK, ReturnCodeEnum.POST_NOT_OPEN.getCode(), message);
    }

    private void unauthorized(HttpServletResponse response) throws Exception {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, 0, "登录过期或未登录");
    }

    private void write(HttpServletResponse response, int httpStatus, int status, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(httpStatus);
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("message", message);
        result.put("timestamp", LocalDateTime.now().toString());
        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
