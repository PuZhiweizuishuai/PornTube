package com.buguagaoshu.tiktube.service;

import com.buguagaoshu.tiktube.cache.WebConfigCache;
import com.buguagaoshu.tiktube.model.MailConfigData;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-05
 */
@Service
@Slf4j
public class MailService {
    private final JavaMailSenderImpl javaMailSender;

    private final WebConfigCache webConfigCache;

    @Autowired
    public MailService(WebConfigCache webConfigCache) {
        this.webConfigCache = webConfigCache;
        this.javaMailSender = new JavaMailSenderImpl();
    }

    /**
     * 初始化邮件配置
     * */
    public void initMainConfig() {
        MailConfigData mailConfig = webConfigCache.getWebConfigData().getMailConfig();
        javaMailSender.setDefaultEncoding("utf-8");
        javaMailSender.setHost(mailConfig.getHost());              // 设置邮箱服务器
        javaMailSender.setPort(mailConfig.getPort());                        // 设置端口
        javaMailSender.setUsername(mailConfig.getUsername());    // 设置用户名
        javaMailSender.setPassword(mailConfig.getPassword());      // 设置密码（记得替换为你实际的密码、授权码）
        javaMailSender.setProtocol(mailConfig.getProtocol());                // 设置协议
        Properties properties = new Properties();           // 配置项
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", true);
        properties.put("mail.smtp.ssl.enable", true);


        javaMailSender.setJavaMailProperties(properties); // 设置配置项
    }

    public boolean sendMail(String to, String subject, String content) {
        // 创建一个邮件消息
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // 创建 MimeMessageHelper
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(webConfigCache.getWebConfigData().getMailConfig().getUsername(), webConfigCache.getWebConfigData().getName());
            // 收件人邮箱
            helper.setTo(to);
            // 邮件标题
            helper.setSubject(subject);
            // 邮件正文，第二个参数表示是否是HTML正文
            helper.setText(content, true);

            // 发送
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    public String verificationCodeInfo(String verificationCode) {
        String html = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>您的验证码</title>
                    <style>
                        body {
                            font-family: 'Roboto', 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #030303;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 0;
                            background-color: #f9f9f9;
                        }
                        .container {
                            background-color: white;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        }
                        .header {
                            background-color: #ff0000;
                            padding: 20px;
                            text-align: center;
                            color: white;
                        }
                        .logo {
                            max-width: 120px;
                            height: auto;
                        }
                        .header h1 {
                            margin: 10px 0 0;
                            font-size: 20px;
                            font-weight: 500;
                        }
                        .content {
                            padding: 25px;
                        }
                        .code-container {
                            margin: 25px 0;
                            text-align: center;
                        }
                        .code {
                            display: inline-block;
                            font-size: 28px;
                            letter-spacing: 3px;
                            padding: 15px 30px;
                            background-color: #f1f1f1;
                            border-radius: 4px;
                            font-weight: 500;
                            color: #ff0000;
                            border-left: 4px solid #ff0000;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #ff0000;
                            color: white;
                            text-decoration: none;
                            border-radius: 2px;
                            font-weight: 500;
                            margin: 15px 0;
                            text-align: center;
                        }
                        .footer {
                            padding: 20px;
                            text-align: center;
                            font-size: 12px;
                            color: #606060;
                            background-color: #f1f1f1;
                        }
                        .note {
                            font-size: 14px;
                            color: #606060;
                            margin-top: 25px;
                            padding-top: 15px;
                            border-top: 1px solid #e0e0e0;
                        }
                        .highlight {
                            color: #ff0000;
                            font-weight: 500;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <!-- 替换为您的网站logo，使用YouTube风格的红色 -->
                            <!--<img src="https://example.com/logo.png" alt="网站Logo" class="logo">-->
                            <h1>邮箱验证</h1>
                        </div>
                
                        <div class="content">
                            <p>尊敬的用户，您好：</p>
                
                            <p>感谢您使用 {{webname}}！请使用以下验证码完成验证：</p>
                
                            <div class="code-container">
                                <div class="code">{{验证码}}</div>
                            </div>
                
                            <p>此验证码将在 <span class="highlight">5分钟</span> 后失效。</p>
                
                            <div class="note">
                                <p><strong>安全提示：</strong>我们的团队永远不会通过邮件、电话或短信向您索要验证码。</p>
                                <p>如果您没有请求此验证码，请忽略此邮件或更改您的账户密码。</p>
                            </div>
                        </div>
                
                        <div class="footer">
                            <p>{{webname2}} © {{年份}}, 保留所有权利.</p>
                            <p><a href="{{url}}" style="color: #606060;">访问我们的网站</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        // 替换占位符
        html = html.replace("{{webname}}", webConfigCache.getWebConfigData().getName())
                .replace("{{验证码}}", verificationCode)
                .replace("{{webname2}}", webConfigCache.getWebConfigData().getName())
                .replace("{{年份}}", String.valueOf(java.time.Year.now().getValue()))
                .replace("{{url}}", webConfigCache.getWebConfigData().getBaseUrl());
        return html;
        // return "欢迎使用：" + webConfigCache.getWebConfigData().getName() + "，你的验证码是：" + verificationCode;
    }
}
