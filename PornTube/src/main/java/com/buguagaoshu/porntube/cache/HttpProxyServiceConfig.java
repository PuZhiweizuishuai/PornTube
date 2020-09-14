package com.buguagaoshu.porntube.cache;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-14 22:00
 */
@Configuration
public class HttpProxyServiceConfig {
    @Bean
    public ServletRegistrationBean<HttpProxyService> servletServletRegistrationBean() {
        HttpProxyService httpProxyService = new HttpProxyService();
        ServletRegistrationBean<HttpProxyService> servletRegistrationBean = new ServletRegistrationBean<>(httpProxyService, "/api/proxy");

        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, "true");
        return servletRegistrationBean;
    }

}
