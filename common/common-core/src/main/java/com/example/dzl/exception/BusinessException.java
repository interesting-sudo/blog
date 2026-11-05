package com.example.dzl.exception;


import com.example.dzl.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String overrideMsg) {
        super(overrideMsg);
        this.errorCode = errorCode;
    }


}
