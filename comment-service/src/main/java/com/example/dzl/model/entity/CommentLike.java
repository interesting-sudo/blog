package com.example.dzl.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("sys_comment_like")
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    private Integer likeStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public CommentLike(Long commentId, Long userId){
        this.commentId = commentId;
        this.userId = userId;
        this.likeStatus = 1;
    }
}
