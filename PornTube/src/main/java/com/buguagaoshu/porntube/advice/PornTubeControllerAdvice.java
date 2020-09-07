package com.buguagaoshu.porntube.advice;

import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.exception.InvitationCodeException;
import com.buguagaoshu.porntube.exception.UserNotFoundException;
import com.buguagaoshu.porntube.exception.VerifyFailedException;
import com.buguagaoshu.porntube.vo.ResponseDetails;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;




/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:31
 */
@RestControllerAdvice(basePackages = {"com.buguagaoshu.porntube.controller"})
public class PornTubeControllerAdvice {


    @ExceptionHandler(value = {Exception.class})
    public ResponseDetails handleException(Exception e) {

        return ResponseDetails.ok(0, e.getMessage());
    }



    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseDetails handleUserException(UserNotFoundException e) {
        return ResponseDetails.ok(ReturnCodeEnum.USER_NOT_FIND).put("data", e.getMessage());
    }

    @ExceptionHandler(value = {VerifyFailedException.class})
    public ResponseDetails handleValidException(VerifyFailedException e) {
        return ResponseDetails.ok(ReturnCodeEnum.VERIFY_FAILED).put("data", e.getMessage());
    }

    @ExceptionHandler(value = {InvitationCodeException.class})
    public ResponseDetails handleInvitationCodeException(InvitationCodeException e) {
        return ResponseDetails.ok(ReturnCodeEnum.INVITATION_ERROR).put("data", e.getMessage());
    }
}
