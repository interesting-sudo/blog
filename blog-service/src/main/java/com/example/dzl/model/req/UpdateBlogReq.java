package com.example.dzl.model.req;

import lombok.Data;

@Data
public class UpdateBlogReq{
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private Long categoryId;
    private String coverImage;
    private String tags;
    private Integer status;
}
