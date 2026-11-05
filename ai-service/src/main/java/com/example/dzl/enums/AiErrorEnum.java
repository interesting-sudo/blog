package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public enum AiErrorEnum implements ErrorCode {
    AI_RATE_LIMITED(50001, HttpStatus.TOO_MANY_REQUESTS, "当前AI服务访问过于频繁，请稍后重试"),
    AI_TIMEOUT(50002, HttpStatus.GATEWAY_TIMEOUT, "AI响应超时，请稍后重试"),
    AI_SERVICE_UNAVAILABLE(50003, HttpStatus.SERVICE_UNAVAILABLE, "AI服务暂不可用，请稍后重试"),
    AI_INVALID_CONTENT(50004, HttpStatus.BAD_REQUEST, "内容为空或无效"),
    AI_MODEL_ERROR(50005, HttpStatus.BAD_GATEWAY, "模型调用失败"),
    AI_TITLE_CANNOT_GENERATE(50006, HttpStatus.BAD_REQUEST, "正文质量太差，无法生成标题，请先整理内容"),
    AI_SUMMARY_CANNOT_GENERATE(50007, HttpStatus.BAD_REQUEST, "正文质量太差或过短，无法生成摘要，请先整理内容");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String msg;

    AiErrorEnum(Integer code, HttpStatus httpStatus, String msg) {
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

