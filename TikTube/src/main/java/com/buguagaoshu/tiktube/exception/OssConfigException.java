package com.buguagaoshu.tiktube.exception;

/**
 * 对象存储配置操作失败
 * 
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-11
 */
public class OssConfigException extends RuntimeException {

    public OssConfigException() {
        super();
    }

    public OssConfigException(String message) {
        super(message);
    }

    public OssConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public OssConfigException(Throwable cause) {
        super(cause);
    }

    protected OssConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
