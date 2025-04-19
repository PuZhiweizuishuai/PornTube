package com.buguagaoshu.tiktube.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
public class MyStringUtils {
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
        return str.substring(0, num);
    }

    public static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 将时间戳转换为 Date 对象
        Date date = new Date(time);
        // 格式化日期
        return sdf.format(date);
    }
}
