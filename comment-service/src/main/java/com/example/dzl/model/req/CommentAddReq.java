package com.example.dzl.model.req;

import lombok.Data;

@Data
public class CommentAddReq {
    private Long blogId;
    private Long userId;
    // 将username 改成 nickname
    private String nickname;
    private String userAvatar;
    private String content;
}
