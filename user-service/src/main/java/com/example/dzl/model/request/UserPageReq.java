package com.example.dzl.model.request;


import lombok.Data;

@Data
public class UserPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String nickname;

}
