package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public enum SearchErrorEnum implements ErrorCode {
    INDEX_FAILED(60001, HttpStatus.INTERNAL_SERVER_ERROR, "索引博客失败"),
    DELETE_FAILED(60002, HttpStatus.INTERNAL_SERVER_ERROR, "删除索引失败");

    private final int code;
    private final HttpStatus httpStatus;
    private final String msg;

    SearchErrorEnum(int code, HttpStatus httpStatus, String msg) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
