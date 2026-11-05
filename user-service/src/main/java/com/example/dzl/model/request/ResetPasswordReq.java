package com.example.dzl.model.request;

import lombok.Data;

@Data
public class ResetPasswordReq {
    /**
     * 用户名
     */
    private String username;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 重置密码token
     */
    private String resetToken;
}
