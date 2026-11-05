package com.example.dzl.model.req;

import lombok.Data;

/**
 * 管理端：评论分页查询请求对象
 */
@Data
public class CommentAdminReq {
    /**
     * 博客ID过滤
     */
    private Long blogId;

    /**
     * 用户昵称搜索关键字
     */
    private String nickname;

    /**
     * 评论内容搜索关键字
     */
    private String content;

    /**
     * 状态过滤: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 当前页码，默认1
     */
    private long pageNum = 1;

    /**
     * 每页大小，默认10
     */
    private long pageSize = 10;
}
