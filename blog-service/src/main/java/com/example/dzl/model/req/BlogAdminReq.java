package com.example.dzl.model.req;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端：博客分页查询请求对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BlogAdminReq extends PageReq {
    /**
     * 标题搜索关键字
     */
    private String title;

    /**
     * 作者名称搜索关键字
     */
    private String authorName;

    /**
     * 标签搜索关键字
     */
    private String tags;

    /**
     * 状态过滤
     */
    private Integer status;
}

