package com.example.dzl.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端：博客列表展示对象
 */
@Data
public class BlogAdminVO {
    private Long id;
    private String title;
    private String authorName;
    private String categoryName;
    private String tags;
    private Integer status;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private LocalDateTime createTime;
}