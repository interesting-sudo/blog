package com.example.dzl.model.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddBlogReq {
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private Long categoryId;
    private String tags;
    private Integer status;
}
