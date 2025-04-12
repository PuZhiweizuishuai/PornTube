package com.buguagaoshu.tiktube.exception;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-01 20:51
 */
public class UserNotLoginException extends RuntimeException {

    public UserNotLoginException() {
        super();
    }

    public UserNotLoginException(String message) {
        super(message);
    }

    public UserNotLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotLoginException(Throwable cause) {
        super(cause);
    }

    protected UserNotLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
