package com.buguagaoshu.tiktube.dto;

import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 19:19
 * 审核
 */
@Data
public class ExamineDto {

    private Long videoId;


    private Boolean result;


    private String type;

    private String message;
}
