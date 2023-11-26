package com.lld.im.service.exception;

import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object  handleException1(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            errorMsg.append(error.getDefaultMessage()).append(",");
        }
        errorMsg.delete(errorMsg.length() - 1, errorMsg.length());

        return ResponseVO.fail(BaseErrorCode.PARAMETER_ERROR.getCode(),
                BaseErrorCode.PARAMETER_ERROR.getError() + " : " + errorMsg.toString());
    }

    @ExceptionHandler(ApplicationException.class)
    public Object applicationExceptionHandler(ApplicationException e) {
        // 使用公共的结果类封装返回结果, 这里我指定状态码为
        return ResponseVO.fail(e.getCode(), e.getError());
    }

    @ExceptionHandler(value=Exception.class)
    public ResponseVO unKnowException(Exception e){
        log.error("统一异常拦截, e: ", e);
        return ResponseVO.fail(BaseErrorCode.SYSTEM_ERROR);
    }

}
