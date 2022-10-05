package com.buguagaoshu.porntube.config;

import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.service.CategoryService;
import com.buguagaoshu.porntube.service.WebSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;


import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
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


    @Autowired
    public WebConfig(LoginInterceptor loginInterceptor, AdminInterceptor adminInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Bean
    public CommandLineRunner dataLoader(WebSettingService webSettingService,
                                        WebSettingCache webSettingCache,
                                        CategoryService categoryService,
                                        CategoryCache categoryCache) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                log.info("开始获取最新的 Web 设置");
                webSettingCache.setWebSettingEntity(webSettingService.getNewSetting());
                log.info("设置获取完成！");
                log.info("开始缓存分类信息");
                categoryCache.setCategoryEntities(categoryService.listWithTree());
                categoryCache.setCategoryEntityMap(categoryService.categoryEntityMap());
                categoryCache.setCategoryMapWithChildren(categoryService.categoryEntityMapWithChildren(categoryCache.getCategoryEntities()));
                log.info("分类信息缓存完成！");
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/api/article/video",
//                        "/api/user/update/**",
//                        "/api/login/log/list")
//                .excludePathPatterns("/api/login", "/api/register", "/api/verifyImage");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/login", 
                    "/api/register",
                    "/api/user/info/**", 
                    "/api/verifyImage", 
                    "/api/web/info",
                    "/api/article/**", 
                    "/api/category/list",
                    "/api/danmaku/v3",
                    "/api/danmaku/v3/**",
                    "/api/upload/file/**"
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**");
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }
}
