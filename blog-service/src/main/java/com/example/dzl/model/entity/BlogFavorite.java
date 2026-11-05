package com.example.dzl.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_blog_favorite")
public class BlogFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long userId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


    public BlogFavorite(Long blogId, Long userId) {
        this.blogId = blogId;
        this.userId = userId;
        this.status = 1;
    }
}
