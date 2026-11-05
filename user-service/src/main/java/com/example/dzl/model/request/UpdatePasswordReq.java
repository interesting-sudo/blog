package com.example.dzl.model.request;

import lombok.Data;

@Data
public class UpdatePasswordReq {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
