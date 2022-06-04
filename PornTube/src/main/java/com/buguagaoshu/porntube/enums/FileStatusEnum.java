package com.buguagaoshu.porntube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-05 0:32
 */
public enum FileStatusEnum {
    /**
     * 文件状态类型
     * */
    NOT_USE_FILE(0, "未使用文件，临时文件，后期自动删除"),

    USED(1, "已使用文件"),

    DELETE(-1, "文件已被删除")
    ;
    int code;
    String msg;

    FileStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
