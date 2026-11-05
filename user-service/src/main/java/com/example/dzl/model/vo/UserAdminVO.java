package com.example.dzl.model.vo;

import lombok.Data;


/**
 * 管理端：精简版用户信息（用于列表展示）
 */
@Data
public class UserAdminVO{

    /**
     * 用户 ID（必须包含，否则后续禁用/重置密码操作无法进行）
     */
    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;
    /**
     * 性别
     */
    private String gender;

    /**
     * 角色 ID
     */
    private Integer roleId;
    /**
     * 用户状态：1-正常，0-禁用
     */
    private Integer status;
    /**
     * 注册时间
     */
    private String createTime;
}
