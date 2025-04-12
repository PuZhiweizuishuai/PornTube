package com.buguagaoshu.tiktube.utils;


import java.util.LinkedList;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-07 17:04
 */
public class DanmakuUtils {
    public static List<Object> createDanmaku(Double time,
                                              Integer type,
                                              Long colorNumber,
                                              String colorHex,
                                              String text) {
        List<Object> list = new LinkedList<>();
        list.add(time);
        list.add(type);
        list.add(colorNumber);
        list.add(colorHex);
        list.add(text);
        return list;
    }
}
