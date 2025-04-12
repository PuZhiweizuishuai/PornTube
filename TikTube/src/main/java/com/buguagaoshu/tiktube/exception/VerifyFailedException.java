package com.buguagaoshu.tiktube.exception;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 16:06
 */
public class VerifyFailedException extends RuntimeException {
    public VerifyFailedException() {
        super();
    }

    public VerifyFailedException(String message) {
        super(message);
    }

    public VerifyFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerifyFailedException(Throwable cause) {
        super(cause);
    }

    protected VerifyFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
