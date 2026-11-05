package com.example.dzl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SendCodeTypeEnum {
    REGISTER(1, "注册"),
    RESET_PASSWORD(2, "重置密码");

    private final Integer code;
    private final String desc;
}
