package com.buguagaoshu.tiktube.utils;

import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
public class MyStringUtils {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

    /**
     * 截取前 N 位字符串
     * */
    public static String extractString(String str, int num) {
        if (str == null) {
            return "";
        }
        if (str.length() <= num) {
            return str;
        }
        return str.substring(0, num) + ",,,";
    }

    /**
     * 转为纯文本：去掉 HTML 标签与尖括号、压缩空白后截断。
     * 用于喂给大模型的待审核内容与模型返回的理由，防止伪造提示词分隔符。
     */
    public static String toPlainText(String text, int maxLength) {
        if (text == null || text.isEmpty() || maxLength <= 0) {
            return "";
        }
        String plain = HTML_TAG.matcher(text).replaceAll(" ");
        plain = plain.replace("<", " ").replace(">", " ");
        plain = plain.replaceAll("[\\p{Cntrl}]", " ").replaceAll("\\s+", " ").trim();
        if (plain.length() > maxLength) {
            plain = plain.substring(0, maxLength);
        }
        return plain;
    }

    /**
     * 转成可以安全拼进富文本消息（Markdown / HTML）的片段，
     * 用户输入与模型输出都必须经此处理，避免存储型 XSS 与可点击的恶意链接。
     */
    public static String safeTextForRichMessage(String text, int maxLength) {
        String plain = toPlainText(text, maxLength);
        if (plain.isEmpty()) {
            return "";
        }
        return HtmlUtils.htmlEscape(plain).replace("[", "\\[").replace("]", "\\]");
    }

    public static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 将时间戳转换为 Date 对象
        Date date = new Date(time);
        // 格式化日期
        return sdf.format(date);
    }
}
