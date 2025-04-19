package com.buguagaoshu.tiktube.utils;

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
}
