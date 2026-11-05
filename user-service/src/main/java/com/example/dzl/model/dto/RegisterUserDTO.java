package com.example.dzl.model.dto;


import lombok.Data;

@Data
public class RegisterUserDTO {
    private String username;
    private String nickname;
    private String password;
    private String avatarUrl;
    private String motto;
    private String gender;
    private Integer roleId;

}
