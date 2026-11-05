package com.example.dzl.model.req;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlogPageReq extends PageReq{
    /**
     * 作者ID
     */
    private Long authorId;
    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 是否只查询已发布的文章
     * true: 只查询已发布（status = 1）
     * false/null: 查询所有状态
     */
    private Boolean onlyPublished;

}
