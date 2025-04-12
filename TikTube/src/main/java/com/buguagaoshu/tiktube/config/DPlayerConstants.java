package com.buguagaoshu.tiktube.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 23:21
 * https://blog.csdn.net/ZPeng_CSDN/article/details/106721975
 */
public class DPlayerConstants {
    public static final int DPLAYER_SUCCESS_CODE = 0;

    public static final String DPLAYER_BARRAGE_ID = "0";
    public static final String DPLAYER_BARRAGE_AUTHOR = "DPlayer";
    public static final String DPLAYER_BARRAGE_TIME = "0";
    public static final String DPLAYER_BARRAGE_TEXT = "";
    public static final String DPLAYER_BARRAGE_COLOR = "16777215";
    public static final String DPLAYER_BARRAGE_TYPE = "0";

    public static final List<String> barrage_init(List<String> data){
        data = new ArrayList<String>();
        data.add(DPLAYER_BARRAGE_TIME);
        data.add(DPLAYER_BARRAGE_TYPE);
        data.add(DPLAYER_BARRAGE_COLOR);
        data.add(DPLAYER_BARRAGE_AUTHOR);
        data.add(DPLAYER_BARRAGE_TEXT);
        return data;
    }
}
