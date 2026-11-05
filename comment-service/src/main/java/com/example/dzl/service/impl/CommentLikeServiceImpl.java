package com.example.dzl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.mapper.CommentLikeMapper;
import com.example.dzl.model.entity.Comment;
import com.example.dzl.model.entity.CommentLike;
import com.example.dzl.service.CommentLikeService;
import com.example.dzl.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
    @Autowired
    private CommentService commentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long commentId, Long userId) {
        log.info("commentId: {}, userId: {}", commentId, userId);
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return false;
        }
        CommentLike record = this.getOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));
        if (record == null) {
            // 新增, 点赞数加1
            record = new CommentLike(commentId, userId);
            this.save(record);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);
            return true;
        }
        // 取消点赞, 点赞数减1
        if (record.getLikeStatus() == 1) {
            record.setLikeStatus(0);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentService.updateById(comment);
            return false;
        } else {
            // 点赞, 点赞数加1
            record.setLikeStatus(1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);
            return true;
        }
    }
    /**
     * 获取评论点赞数
     */
    @Override
    public long countLikes(Long commentId) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return 0;
        }
        return comment.getLikeCount();
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        CommentLike record = this.getOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
                .eq(CommentLike::getLikeStatus, 1));
        return record != null;
    }

    @Override
    public List<Long> getLikedCommentIds(Long blogId, Long userId) {
        // 1. 先查出该博客下的所有评论ID
        List<Comment> comments = commentService.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getBlogId, blogId)
                // 只查询启用的评论
                .eq(Comment::getStatus, 1)
                .select(Comment::getId));

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 2. 再查出这些评论中当前用户点赞过的
        List<CommentLike> likes = this.list(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .eq(CommentLike::getLikeStatus, 1)
                .in(CommentLike::getCommentId, commentIds)
                .select(CommentLike::getCommentId));

        return likes.stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());
    }
}
