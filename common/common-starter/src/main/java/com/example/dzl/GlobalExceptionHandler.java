package com.example.dzl;

import com.example.dzl.enums.CommonErrorEnum;
import com.example.dzl.enums.ErrorCode;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author 光的代言人
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result handleNotFound(NoHandlerFoundException ex) {
        return Result.error(HttpStatus.NOT_FOUND.value(), "资源未找到，可能是请求路径错误，请检查！");
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParam(MissingServletRequestParameterException ex) {
        log.error("缺少必要请求参数：{}", ex.getParameterName(), ex);
        return Result.error(HttpStatus.BAD_REQUEST.value(), "缺少必要的请求参数：" + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("请求参数类型不匹配：param={}, requiredType={}", ex.getName(), ex.getRequiredType(), ex);
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求参数类型不匹配：" + ex.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        // 返回自定义的错误消息
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "请求方法不匹配，请检查请求方式!");
    }
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleCustomException(BusinessException ex) {
        // 返回自定义异常 Result 格式，包含 code 和 msg
        Result<Object> result = Result.error(ex.getErrorCode().getCode(), ex.getMessage());
        return new ResponseEntity<>(result, ex.getErrorCode().getHttpStatus());
    }
    /**
     * 处理所有其他异常（兜底异常）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleGeneralException(Exception ex) {
        log.error("系统异常：", ex);
        // 对于通用异常，返回系统错误信息
        ErrorCode errorCode = CommonErrorEnum.INTERNAL_ERROR;
        Result<Object> result = Result.error(errorCode.getCode(), errorCode.getMsg());
        return new ResponseEntity<>(result, errorCode.getHttpStatus());
    }
}

