package com.example.dzl.model.request;


import lombok.Data;

@Data
public class UpdateUserReq {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private String gender;

    /**
     * 座右铭
     */
    private String motto;

    /**
     * 头像URL (如果上传了新头像，后端会自动填充此字段)
     */
    private String avatarUrl;
}

