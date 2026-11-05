package com.example.dzl.model.dto;


import lombok.Data;

@Data
public class UpdateBlogDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String authorAvatar;
    private String tags;
    private String coverImage;
    private Integer status;
}
