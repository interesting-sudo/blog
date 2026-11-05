package com.example.dzl.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("sys_comment")
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 博客id
     */
    private Long blogId;
    /**
     * 根评论id
     */
    private Long rootId;
    /**
     * 父评论id
     */
    private Long parentId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 昵称（冗余字段）
     */
    private String nickname;
    /**
     * 用户头像（冗余字段）
     */
    private String userAvatar;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 状态:0-禁用,1-启用
     */
    private Integer status;
    /**
     * 点赞数
     */
    private Long likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
