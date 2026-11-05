package com.example.dzl.enums;


import org.springframework.http.HttpStatus;

/**
 * 文档服务错误枚举
 */
public enum DocumentErrorEnum implements ErrorCode {
    FILE_EMPTY(40001, HttpStatus.BAD_REQUEST, "文件不能为空"),
    FILE_NAME_EMPTY(40002, HttpStatus.BAD_REQUEST, "文件名不能为空"),
    FILE_TYPE_NOT_SUPPORTED(40003, HttpStatus.BAD_REQUEST, "不支持的文件类型，仅支持图片格式（jpg、jpeg、png、gif、webp）"),
    FILE_CONTENT_TYPE_NOT_SUPPORTED(40004, HttpStatus.BAD_REQUEST, "不支持的文件类型(Content-Type)，仅支持图片格式（jpg、jpeg、png、gif、webp）"),
    FILE_URL_EMPTY(40005, HttpStatus.BAD_REQUEST, "文件URL不能为空"),
    FILE_UPLOAD_FAILED(40006, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败"),
    FILE_DELETE_FAILED(40007, HttpStatus.INTERNAL_SERVER_ERROR, "文件删除失败"),
    FILE_NOT_EXIST(40008, HttpStatus.NOT_FOUND, "文件不存在或已删除"),
    ;

    private final Integer code; // 业务错误码（可自定义，非HTTP状态码）
    private final HttpStatus httpStatus; // HTTP状态码枚举
    private final String msg; // 默认提示文案

    DocumentErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
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

