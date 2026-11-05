package com.example.dzl.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenVO {
    private String accessToken;
    // 辅助字段，提醒前端什么时候过期，提前做准备
    private Long expiresIn;
    // 角色 ID，辅助前端进行权限按钮展示
    private Integer roleId;

    public AccessTokenVO(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
