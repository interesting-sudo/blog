package com.example.dzl.enums;

import lombok.Getter;

/**
 * 系统角色枚举
 * 统一管理角色名称/编码，避免硬编码
 */
@Getter
public enum RoleEnum {
    /**
     * 普通用户角色
     */
    USER("USER", "普通用户"),
    /**
     * 管理员角色
     */
    ADMIN("ADMIN", "系统管理员")
    ;

    /**
     * 角色编码（与数据库role_name字段一致）
     */
    private final String roleName;
    /**
     * 角色描述
     */
    private final String roleDesc;

    RoleEnum(String roleName, String roleDesc) {
        this.roleName = roleName;
        this.roleDesc = roleDesc;
    }

    /**
     * 根据角色编码匹配枚举（容错处理）
     */
    public static RoleEnum getByRoleCode(String roleCode) {
        for (RoleEnum role : values()) {
            if (role.getRoleName().equals(roleCode)) {
                return role;
            }
        }
        return null;
    }
}
