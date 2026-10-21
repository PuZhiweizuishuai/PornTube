package com.buguagaoshu.tiktube.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
     * 图形验证码的键，服务端下发、浏览器自动回传
     * */
    public static final String COOKIE_VERIFY_KEY = "COOKIE-VERIFY-KEY";

    /**
     * JWT 密钥
     * 你指定的密钥字节数组仅有 144 位，而对于任何 JWT HMAC - SHA 算法来说，这是不够安全的。根据 JWT JWA 规范（RFC 7518，第 3.2 节），
     * 使用 HMAC - SHA 算法的密钥大小必须大于或等于 256 位（密钥大小必须大于或等于哈希输出大小）
     * Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
     * Keys.hmacShaKeyFor("你指定的密钥字节数组仅有 144 位，而对于任何 JWT HMAC - SHA 算法来说，这是不够安全的。根据 JWT JWA 规范（RFC 7518，第 3.2 节），".getBytes(StandardCharsets.UTF_8));
     * Jwts.SIG.HS512.key().build()
     *
     *  Keys.hmacShaKeyFor 为开发时使用的固定密钥
     *
     *  实际部署请使用 Jwts.SIG.HS512.key().build(); 随机生成密钥或自行更换密钥
     * */
    // public final static SecretKey SECRET_KEY = Keys.hmacShaKeyFor("你指定的密钥字节数组仅有 144 位，而对于任何 JWT HMAC - SHA 算法来说，这是不够安全的。根据 JWT JWA 规范（RFC 7518，第 3.2 节），".getBytes(StandardCharsets.UTF_8));
    public final static SecretKey SECRET_KEY = Jwts.SIG.HS512.key().build();

    /**
     * AES 加密密钥
     * 使用 UUID 每次启动随机生成
     * UUID.randomUUID().toString()
     * */
    public final static String AES_KEY = UUID.randomUUID().toString();


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


    public final static int HOT_NUM = 100;

    /**
     * OSS 对象存储访问链接有效期
     * */
    public final static int OSS_DAY_TIME = 1;

    /**
     * 账号密码错误最大次数
     * */
    public final static int MAX_ATTEMPTS = 3;


    /**
     * 锁定时间（分钟）
     * */
    public static final long LOCK_DURATION = 60;


    /**
     * 一个简单的接口限流配置
     * 对稿件，评论，弹幕发布接口进行限流
     * 避免被统一用户向数据库大量写入数据
     * */
    public final static int SIMPLE_CURRENT_LIMITING_TYPE_ARTICLE = 1;
    public final static int SIMPLE_CURRENT_LIMITING_TYPE_COMMENT = 2;
    public final static int SIMPLE_CURRENT_LIMITING_TYPE_DANMAKU = 3;
    public final static int SIMPLE_CURRENT_LIMITING_TYPE_ARTICLE_PASSWORD = 4;
}
