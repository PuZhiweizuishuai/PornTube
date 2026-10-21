package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.service.AIConfigServer;
import com.buguagaoshu.tiktube.service.CategoryService;
import com.buguagaoshu.tiktube.service.WebConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;


import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 17:51
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {


    private final LoginInterceptor loginInterceptor;

    private final AdminInterceptor adminInterceptor;

    private final VipInterceptor vipInterceptor;
    
    private final MyConfigProperties myConfigProperties;

    /**
     * 默认文件保存位置 0 为 本地
     * 其它数字依照数据库中 OSS 配置文件判断
     * 开启 open-redis 后由调度线程跨实例同步，上传线程要能立刻看见新值
     * */
    public static volatile int FILE_SAVE_LOCATION = 0;


    @Autowired
    public WebConfig(LoginInterceptor loginInterceptor,
                     AdminInterceptor adminInterceptor,
                     VipInterceptor vipInterceptor,
                     MyConfigProperties myConfigProperties) {
        this.loginInterceptor = loginInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.vipInterceptor = vipInterceptor;
        this.myConfigProperties = myConfigProperties;
    }

    @Bean
    public CommandLineRunner dataLoader(WebConfigService webConfigService,
                                        CategoryService categoryService,
                                        AIConfigServer aiConfigServer) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                log.info("开始缓存分类信息");
                categoryService.refreshCache();
                log.info("分类信息缓存完成！");
                log.info("开始获取最新的 Web 设置");
                webConfigService.refreshCache();
                log.info("设置获取完成！");
                // 输出Redis状态信息
                if (myConfigProperties.getOpenRedis()) {
                    log.info("Redis缓存已启用");
                } else {
                    log.info("Redis缓存未启用，系统将使用内存缓存");
                }
                aiConfigServer.setCacheConfig();
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/version",
                    "/api/login",
                    "/api/login/totp",
                    "/api/register",
                    "/api/user/info/**",
                    "/api/user/list/search",
                    "/api/verifyImage",
                    "/api/verify/send",
                    "/api/user/forgot/password",
                    "/api/web/info",
                    "/api/web/notice",
                    "/api/web/notice/**",
                    "/api/article/**", 
                    "/api/category/**",
                    "/api/danmaku/**",
                    "/api/favorites/label/public/**",
                    "/api/upload/file/**",
                    "/api/upload/**/oss/**",
                    "/api/comment/**"
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/api/admin/**"
                );


        // 投稿接口权限拦截：是否放行普通用户由「系统设置 - 投稿权限设置」决定，
        // 公网演示时请在后台关闭，不要再注释这里的注册配置
        registry.addInterceptor(vipInterceptor)
                .addPathPatterns(VipInterceptor.postApiPaths());
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }

}
