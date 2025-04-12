package com.buguagaoshu.tiktube.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-06-10 11:52
 * 因为前端富文本编辑器采用了 vditor 这个编辑器
 * 所以文件上传返回数据需要符合 vditor 设定
 * 如果需要其它上传返回数据方式，需要修改 vditor 的代码
 *
 */
@Data
public class VditorFiles {
    private String msg;

    private Integer code;

    private Map<String, Object> data;
}
