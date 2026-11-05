package com.example.dzl.model.req;

import lombok.Data;

@Data
public class CommentPageReq {
    private Long blogId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
