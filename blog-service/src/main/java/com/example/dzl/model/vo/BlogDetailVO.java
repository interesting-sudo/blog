package com.example.dzl.model.vo;

import lombok.Data;


@Data
public class BlogDetailVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
//    private Long authorId;
    private String authorName;
    private String authorAvatar;
//    private Long categoryId;
    private String categoryName; // 额外增加分类名称方便展示
    private String tags;
    private String coverImage;
    private Integer status;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private String createTime;
//    private String updateTime;
}
