package com.example.dzl.enums;


import org.springframework.http.HttpStatus;

public enum UserErrorEnum implements ErrorCode{
    USER_NOT_EXIST(10001, HttpStatus.BAD_REQUEST, "用户不存在"),
    USER_ALREADY_EXIST(10002, HttpStatus.BAD_REQUEST, "用户名已存在"),
    USER_NICKNAME_REPEAT(10003, HttpStatus.BAD_REQUEST, "用户昵称重复"),
    USER_PASSWORD_ERROR(10004, HttpStatus.BAD_REQUEST, "用户密码错误"),
    USER_OLD_PASSWORD_ERROR(10004, HttpStatus.BAD_REQUEST, "原密码输入错误"),
    USER_DISABLED(10005, HttpStatus.FORBIDDEN, "用户被禁用，请联系管理员后重试！"),
    ROLE_NOT_EXIST(10006, HttpStatus.NOT_FOUND, "角色不存在"),
    EMAIL_SEND_FAIL(10007, HttpStatus.INTERNAL_SERVER_ERROR, "邮件发送失败"),
    VERIFICATION_CODE_ERROR(10008, HttpStatus.BAD_REQUEST, "验证码错误或已失效"),
    EMAIL_ALREADY_REGISTERED(10009, HttpStatus.BAD_REQUEST, "该邮箱已经被注册"),
    ADMIN_CANNOT_MODIFY_SELF(10010, HttpStatus.BAD_REQUEST, "管理员不能修改自己的状态或重置自己的密码"),


    ;

    private final Integer code; // 业务错误码（可自定义，非HTTP状态码）
    private final HttpStatus httpStatus; // HTTP状态码枚举
    private final String msg; // 默认提示文案

    UserErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
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
