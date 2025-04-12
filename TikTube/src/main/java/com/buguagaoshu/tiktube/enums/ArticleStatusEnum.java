package com.buguagaoshu.tiktube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 22:19
 */
public enum ArticleStatusEnum {
    DELETE(1),
    NORMAL(0);
    int code;

    ArticleStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
