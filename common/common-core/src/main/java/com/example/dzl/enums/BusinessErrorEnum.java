package com.example.dzl.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorEnum {
    // ========== 通用错误 ==========
    FAILED(500, HttpStatus.INTERNAL_SERVER_ERROR, "操作失败"),
    RESOURCE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "资源未找到"),
    PARAM_ERROR(400, HttpStatus.BAD_REQUEST, "请求参数错误"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "未登录或token过期"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "无权限访问"),


    // ========== 业务专属错误（博客系统） ==========
    USER_NOT_EXIST(1001, HttpStatus.BAD_REQUEST, "用户不存在"),
    USER_ALREADY_EXIST(1002, HttpStatus.BAD_REQUEST, "用户名已存在"),
    USER_NICKNAME_REPEAT(1003, HttpStatus.BAD_REQUEST, "用户昵称重复"),
    USER_PASSWORD_ERROR(1004, HttpStatus.BAD_REQUEST, "用户密码错误"),
    USER_DISABLED(1005, HttpStatus.FORBIDDEN, "用户被禁用，请联系管理员后重试！"),
    ROLE_NOT_EXIST(1006, HttpStatus.NOT_FOUND, "角色不存在"),

    BLOG_TITLE_EMPTY(2001, HttpStatus.BAD_REQUEST, "博客标题不能为空"),
    BLOG_NOT_EXIST(2002, HttpStatus.NOT_FOUND, "博客不存在"),
    AI_SERVICE_ERROR(3001, HttpStatus.INTERNAL_SERVER_ERROR, "AI辅助服务调用失败"),

    ;

    // 枚举属性
    private final Integer code; // 业务错误码（可自定义，非HTTP状态码）
    private final HttpStatus httpStatus; // HTTP状态码枚举
    private final String msg; // 默认提示文案

    // 构造器（枚举类构造器默认私有）
    BusinessErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.msg = msg;
    }

}
