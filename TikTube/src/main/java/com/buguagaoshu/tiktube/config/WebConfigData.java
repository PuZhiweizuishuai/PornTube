package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.model.MailConfigData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-05
 * 存储系统配置
 * web_setting 表已经弃用
 * 采用 web_config 保存配置文件
 */
@Slf4j
@Data
public class WebConfigData {
    /**
     * 网站名
     */
    private String name;

    private String baseUrl = "";

    /**
     * 是否开启非vip每日观看次数限制
     * TODO 待实现
     */
    private Boolean openNoVipLimit = false;

    /**
     * 非vip 每日观看次数
     * TODO 待实现
     */
    private Boolean noVipViewCount = false;

    /**
     * 网页logo地址
     */
    private String logoUrl;

    /**
     * 是否开启邀请码注册
     */
    private Boolean openInvitationRegister = false;

    /**
     * 投稿权限总开关：是否允许普通用户（非 VIP、非管理员）投稿
     * 关闭后普通用户连修改、删除自己已发布的内容一起被禁止，系统对普通用户完全只读，
     * 适合把系统作为演示环境部署到公网
     */
    private Boolean openNormalUserPost = false;

    /**
     * 总开关打开时，是否允许普通用户投稿视频
     */
    private Boolean openNormalUserPostVideo = false;

    /**
     * 总开关打开时，是否允许普通用户投稿文章
     */
    private Boolean openNormalUserPostArticle = false;

    /**
     * 网页简短的描述
     */
    private String webDescribe;

    /**
     * 是否开启每日上传视频增加非会员观看次数
     * TODO 待实现
     */
    private Boolean openUploadVideoAddViewCount = false;

    /**
     * 是否开启视频，文章，图片审核
     */
    private Boolean openExamine = true;

    /**
     * 是否开启评论审核
     * */
    private Boolean openCommentExam = true;

    /**
     * 是否开启弹幕审核
     * */
    private Boolean openDanmakuExam = true;


    private Integer homeMaxVideoCount;

    /**
     * 是否开启邮箱设置
     * */
    private Boolean openEmail = false;

    /**
     * 是否开启 AI 配置
     * */
    private Boolean openAIConfig = false;


    private MailConfigData mailConfig;


    /**
     * 设置默认配置
     * */
    public static WebConfigData defaultConfig() {
        WebConfigData webConfigData = new WebConfigData();
        webConfigData.setName("TikTube");
        webConfigData.setOpenNoVipLimit(false);
        webConfigData.setNoVipViewCount(false);
        webConfigData.setOpenInvitationRegister(false);
        webConfigData.setOpenNormalUserPost(false);
        webConfigData.setOpenNormalUserPostVideo(false);
        webConfigData.setOpenNormalUserPostArticle(false);
        webConfigData.setWebDescribe("一个牛逼的视频网站!");
        webConfigData.setOpenUploadVideoAddViewCount(false);
        webConfigData.setOpenExamine(true);
        webConfigData.setHomeMaxVideoCount(50);
        webConfigData.setOpenEmail(false);
        webConfigData.setLogoUrl("/favicon.jpg");
        webConfigData.setOpenCommentExam(true);
        webConfigData.setOpenDanmakuExam(true);
        webConfigData.setOpenAIConfig(false);
        webConfigData.setMailConfig(new MailConfigData());
        return webConfigData;

    }

    /**
     * 拷贝对象
     * */
    public WebConfigData copy() {
        WebConfigData webConfigData = new WebConfigData();
        BeanUtils.copyProperties(this, webConfigData);
        return webConfigData;
    }


    /**
     * 数据脱敏
     * */
    public WebConfigData clean() {
        this.mailConfig = null;
        this.openAIConfig = null;
        return this;
    }

    /**
     * 创建保存到数据库的数据
     * */
    public static String createSaveData(WebConfigData webConfigData) {
        ObjectMapper objectMapper = new ObjectMapper();
        String defaultConfig = null;
        try {
            defaultConfig = objectMapper.writeValueAsString(defaultConfig());
            return objectMapper.writeValueAsString(webConfigData);
        } catch (Exception e) {
            log.error("将对象转换为 JSON 字符串异常：{}", e.getMessage());
            // 发生异常返回默认配置
            return defaultConfig;
        }
    }

    public static WebConfigData analysisData(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, WebConfigData.class);
        } catch (Exception e) {
            log.error("配置文件反序列化异常：{}，系统将返回默认配置。", e.getMessage());
            // 读取失败返回默认配置
            return defaultConfig();
        }
    }
}
