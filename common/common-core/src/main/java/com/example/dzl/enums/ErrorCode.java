package com.example.dzl.enums;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    int getCode();
    String getMsg();
    HttpStatus getHttpStatus();
}

