package com.example.dzl.model.dto;

import lombok.Data;

@Data
public class UserInfoDTO {

    private Long userId;
    private String username;
    private String nickname;
    private String gender;
    private String motto;
    private String avatarUrl;
    private String createTime;


}
