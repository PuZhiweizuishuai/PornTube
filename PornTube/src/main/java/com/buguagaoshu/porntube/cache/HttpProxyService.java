package com.buguagaoshu.porntube.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-14 21:53
 */
@Slf4j
@Service
public class HttpProxyService extends ProxyServlet {
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void initTarget() throws ServletException {
        // 默认代理到百度
        this.targetUri = "https://www.baidu.com/";
        if (this.targetUri == null) {
            throw new ServletException("targetUri is required.");
        } else {
            try {
                this.targetUriObj = new URI(this.targetUri);
            } catch (Exception var2) {
                throw new ServletException("Trying to process targetUri init parameter: " + var2, var2);
            }

            this.targetHost = URIUtils.extractHost(this.targetUriObj);
        }
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        String[] link = servletRequest.getParameterMap().get("url");
        String targetUri = "https://www.baidu.com";
        if (link != null) {
            for (String s: link) {
                targetUri = s;
            }
        }


        servletRequest.setAttribute(ATTR_TARGET_URI, null);
        super.targetUri = targetUri;

        URI uri = null;
        try {
            uri = new URI(targetUri);
        } catch (URISyntaxException e) {
            log.error("创建URI对象出错, targetUri[{}]", targetUri, e);
        }
        servletRequest.setAttribute(ATTR_TARGET_HOST, null);
        super.targetHost = URIUtils.extractHost(uri);

        super.service(servletRequest, servletResponse);
    }
}
