package com.example.dzl.model.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@TableName("sys_blog_tag")
@AllArgsConstructor
public class BlogTag {
    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 标签ID
     */
    private Long tagId;
}
