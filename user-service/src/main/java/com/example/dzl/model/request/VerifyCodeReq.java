package com.example.dzl.model.request;

import lombok.Data;

@Data
public class VerifyCodeReq {
    private String email;
    private String code;
}