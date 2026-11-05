package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public enum CommonErrorEnum implements ErrorCode{
    // 400xx：请求通用错误
    PARAM_ERROR(40000, HttpStatus.BAD_REQUEST, "请求参数错误"),
    VALIDATION_FAILED(40001, HttpStatus.BAD_REQUEST, "参数校验失败"),

    // 401xx：认证
    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "未登录或登录已过期"),

    // 403xx：鉴权
    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "权限不足"),

    // 404xx：资源通用
    NOT_FOUND(40400, HttpStatus.NOT_FOUND, "资源不存在"),

    // 429xx：限流
    TOO_MANY_REQUESTS(42900, HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试"),

    // 500xx：系统通用错误
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "系统错误，请联系管理员后重试！"),
    SERVICE_UNAVAILABLE(50300, HttpStatus.SERVICE_UNAVAILABLE, "服务暂不可用");

    private final int code;
    private final HttpStatus httpStatus;
    private final String msg;

    CommonErrorEnum(int code, HttpStatus httpStatus, String msg) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.msg = msg;
    }

    @Override
    public int getCode() { return code; }
    @Override
    public String getMsg() { return msg; }
    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }
}
