package com.example.dzl.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String nickname;
    private String password;

    private String gender;
    private String motto;
    private String avatarUrl;
    private Integer roleId;
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
