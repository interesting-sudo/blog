package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.entity.CommentLike;

import java.util.List;

public interface CommentLikeService extends IService<CommentLike> {
    boolean toggleLike(Long commentId, Long userId);
    long countLikes(Long commentId);

    boolean isLiked(Long commentId, Long userId);

    List<Long> getLikedCommentIds(Long blogId, Long userId);
}
