package com.example.dzl.model.vo;

import lombok.Data;

@Data
public class CommentVO {
    private Long id;
    private Long blogId;
    private String blogTitle;
    private Long rootId;
    private Long parentId;
    private Long userId;
    // 将username 改成 nickname
    private String nickname;
    /** 用户头像（与评论表冗余字段一致） */
    private String userAvatar;
    private String content;
    private Integer status;
    private Long likeCount;
    private String createTime;
}
