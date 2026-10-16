package com.buguagaoshu.tiktube.dto;

import com.buguagaoshu.tiktube.valid.ListValue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 19:19
 * 审核
 */
@Data
public class ExamineDto {

    @NotNull(message = "缺少稿件ID")
    private Long videoId;


    @NotNull(message = "缺少审核结果")
    private Boolean result;


    @NotNull(message = "缺少审核类型")
    @ListValue(value = {0,1,2,3,4,20})
    private Integer type;

    @Size(max = 1000, message = "审核意见过长")
    private String message;
}
