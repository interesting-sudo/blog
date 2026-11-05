package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public enum BlogErrorEnum implements ErrorCode{
    // 用户不存在
    USER_NOT_EXIST(20001, HttpStatus.NOT_FOUND, "用户不存在"),
    BLOG_TITLE_EMPTY(20001, HttpStatus.BAD_REQUEST, "博客标题不能为空"),
    BLOG_NOT_EXIST(20002, HttpStatus.NOT_FOUND, "博客不存在"),
    CATEGORY_NOT_EXIST(20003, HttpStatus.NOT_FOUND, "分类不存在"),
    BLOG_NOT_PUBLISHED(20004, HttpStatus.FORBIDDEN, "博客未发布"),
    BLOG_NOT_LIKE(20005, HttpStatus.FORBIDDEN, "博客未点赞"),
    BLOG_ALREADY_LIKE(20006, HttpStatus.FORBIDDEN, "博客已点赞"),
    BLOG_ALREADY_FAVORITE(20007, HttpStatus.FORBIDDEN, "博客已收藏"),
    BLOG_NOT_FAVORITE(20008, HttpStatus.FORBIDDEN, "博客未收藏"),
    CATEGORY_HAS_BLOGS(20009, HttpStatus.BAD_REQUEST, "该分类下尚有文章，无法删除"),
    TAG_HAS_BLOGS(20010, HttpStatus.BAD_REQUEST, "该标签下尚有文章，无法删除"),

    ;

    private final Integer code; // 业务错误码（可自定义，非HTTP状态码）
    private final HttpStatus httpStatus; // HTTP状态码枚举
    private final String msg; // 默认提示文案

    BlogErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
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
