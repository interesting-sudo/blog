package com.example.dzl.model.dto;


import lombok.Data;

@Data
public class BlogIndexDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long categoryId;
    private String categoryName;
    private String tags;
    private String coverImage;
    private Integer status;
    private Long viewCount;
    private Long likeCount;
    private String createTime;
    private String updateTime;
}

