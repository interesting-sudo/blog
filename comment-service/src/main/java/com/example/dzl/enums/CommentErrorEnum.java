package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public enum CommentErrorEnum implements ErrorCode {
    // 将username 改成 nickname
    COMMENT_NICKNAME_EMPTY(30001, HttpStatus.BAD_REQUEST, "昵称不能为空"),
    COMMENT_CONTENT_EMPTY(30003, HttpStatus.BAD_REQUEST, "评论内容不能为空"),
    COMMENT_BLOG_ID_EMPTY(30004, HttpStatus.BAD_REQUEST, "博客ID不能为空"),
    COMMENT_ROOT_ID_EMPTY(30005, HttpStatus.BAD_REQUEST, "根评论ID不能为空"),
    COMMENT_PARENT_ID_EMPTY(30006, HttpStatus.BAD_REQUEST, "父评论ID不能为空"),
    COMMENT_USER_ID_EMPTY(30007, HttpStatus.BAD_REQUEST, "用户ID不能为空"),
    COMMENT_USER_AVATAR_EMPTY(30008, HttpStatus.BAD_REQUEST, "用户头像不能为空"),
    COMMENT_NOT_FOUND(30009, HttpStatus.NOT_FOUND, "评论不存在"),
    COMMENT_REPLY_FAILED(30010, HttpStatus.INTERNAL_SERVER_ERROR, "回复评论失败"),
    COMMENT_DELETE_FAILED(30011, HttpStatus.INTERNAL_SERVER_ERROR, "删除评论失败"),
    COMMENT_LIKE_FAILED(30012, HttpStatus.INTERNAL_SERVER_ERROR, "点赞评论失败"),
    COMMENT_UNLIKE_FAILED(30013, HttpStatus.INTERNAL_SERVER_ERROR, "取消点赞评论失败"),
    COMMENT_REPLY_SELF_ERROR(30014, HttpStatus.BAD_REQUEST, "不能回复自己的评论"),
    COMMENT_PARENT_NOT_EXIST(30015, HttpStatus.BAD_REQUEST, "回复的评论不存在"),
    COMMENT_SENSITIVE_WORD(30016, HttpStatus.BAD_REQUEST, "评论内容包含敏感词，请修改后重试"),
    ;

    private final Integer code; // 业务错误码（可自定义，非HTTP状态码）
    private final HttpStatus httpStatus; // HTTP状态码枚举
    private final String msg; // 默认提示文案

    CommentErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
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
