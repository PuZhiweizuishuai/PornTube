package com.buguagaoshu.tiktube.config;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-11 14:48
 */
public class WebConstant {
    /**
     * TOKEN 名
     * */
    public static final String COOKIE_TOKEN = "COOKIE-TOKEN";

    /**
     * JWT 密钥
     * */
    public final static String SECRET_KEY = "adfads@44$q232343#";


    /**
     * AES 加密密钥
     * */
    public final static String AES_KEY = "dfdsj#$^!##kjo()r458";


    /**
     * AES 过期时间
     * 24 小时
     */
    public final static Long KEY_EXPIRY_DATE = 86400000L;

    /**
     * 角色Key
     * */
    public final static String ROLE_KEY = "authorities";

    /**
     * Vip 结束时间 Key
     * */
    public final static String VIP_STOP_TIME_KEY = "vipStopTime";

    /**
     * 用户ID
     * */
    public final static String USER_ID = "userId";


    public final static String FILE_CACHE = "file";

    /**
     * 临时文件弃用时间,单位毫秒
     * 默认一周 604800000L
     * */
    public final static long DEPRECATED_FILE_TIME = 604800000L;


    public final static String SYSTEM_CREATE_SCREENSHOT = "系统生成截图";
}
