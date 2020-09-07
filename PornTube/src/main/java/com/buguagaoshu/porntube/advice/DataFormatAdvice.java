package com.buguagaoshu.porntube.advice;

import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:19
 */
@RestControllerAdvice(basePackages = {"com.buguagaoshu.porntube.controller"})
public class DataFormatAdvice {
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseDetails handleValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        HashMap<String, String> map = new HashMap<>();
        result.getFieldErrors().forEach((item)->{
            map.put(item.getField(), item.getDefaultMessage());
        });
        return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION).put("data", map);
    }
}
