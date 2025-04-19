package com.buguagaoshu.tiktube.dto;

import com.buguagaoshu.tiktube.valid.ListValue;
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


    @ListValue(value = {0,1,2,3,4,20})
    private Integer type;

    private String message;
}
