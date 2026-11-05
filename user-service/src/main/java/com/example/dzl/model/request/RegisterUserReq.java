package com.example.dzl.model.request;

import lombok.Data;

@Data
public class RegisterUserReq {
    private String username;
    private String nickname;
    private String password;
    private String motto;
    private String gender;
    private String avatarUrl;
}
