package com.example.dzl.model.req;

import lombok.Data;

@Data
public class BlogSearchReq {
    private String keyword;
    private Long categoryId;
    private Long authorId;
    private String sortBy;
    private String sortOrder;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
