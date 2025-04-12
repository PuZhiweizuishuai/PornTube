package com.buguagaoshu.tiktube.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 16:41
 * 对字段进行校验
 */
public class VerifyFieldUtil {
    public static boolean phoneNumber(String phone) {
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }
}
