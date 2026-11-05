package com.example.dzl.model.vo;

import lombok.Data;

@Data
public class BlogSearchVO {
    private Long id;
    private String title;
    private String summary;
    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private String tags;
    private String coverImage;
    private Long viewCount;
    private Long likeCount;
    private String createTime;
}