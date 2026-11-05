
package com.example.dzl.model.req;

import lombok.Data;

@Data
public class CommentReplyReq {
    private Long blogId;
    private Long rootId;
    private Long parentId;
    private Long userId;
    // 将username 改成 nickname
    private String nickname;
    private String userAvatar;
    private String content;
}

