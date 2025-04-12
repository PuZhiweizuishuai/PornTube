package com.buguagaoshu.tiktube.exception;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 18:02
 */
public class InvitationCodeException extends RuntimeException {
    public InvitationCodeException() {
        super();
    }

    public InvitationCodeException(String message) {
        super(message);
    }

    public InvitationCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvitationCodeException(Throwable cause) {
        super(cause);
    }

    protected InvitationCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
