package com.buguagaoshu.porntube.dto;

import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-07 15:50
 */
@Data
public class DanmakuDto {
    /**
     * 弹幕作者
     * */
    private String author;

    /**
     * 弹幕颜色
     * */
    private Long color;

    /**
     * 弹幕所属视频 ID
     * */
    private Long id;

    /**
     * 正文
     * */
    private String text;

    /**
     * 时间
     * */
    private Double time;

    /**
     * TOKEN
     * */
    private String token;

    /**
     * 类型
     * */
    private Integer type;
}
