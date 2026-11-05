package com.example.dzl.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.req.CommentAddReq;
import com.example.dzl.model.req.CommentPageReq;
import com.example.dzl.model.req.CommentReplyReq;
import com.example.dzl.model.vo.CommentVO;
import com.example.dzl.service.CommentLikeService;
import com.example.dzl.service.CommentService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentLikeService commentLikeService;


    @PostMapping("/add")
    public Result<Void> add(@RequestBody CommentAddReq req) {
        commentService.add(req);
        return Result.success();
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestBody CommentReplyReq req) {
        commentService.reply(req);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @PostMapping("/list")
    public Result<Page<CommentVO>> list(@RequestBody CommentPageReq req) {
        Page<CommentVO> page = commentService.page(req);
        return Result.success(page);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{commentId}/toggleLike")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long commentId, @RequestParam Long userId) {
        boolean isLiked = commentLikeService.toggleLike(commentId, userId);
        long likeCount = commentLikeService.countLikes(commentId);
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);
        data.put("likeCount", likeCount);
        return Result.success(data);
    }



    /**
     * 获取点赞数
     */
    @GetMapping("/{commentId}/likeCount")
    public Result<Long> likeCount(@PathVariable Long commentId) {
        return Result.success(commentLikeService.countLikes(commentId));
    }

    /**
     * 查询是否点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    @GetMapping("/{commentId}/isLiked")
    public Result<Boolean> isLiked(@PathVariable Long commentId, @RequestParam Long userId) {
        return Result.success(commentLikeService.isLiked(commentId, userId));
    }

    /**
     * 获取文章下用户的已点赞的评论 ID 列表
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @GetMapping("/listLiked/{blogId}")
    public Result<List<Long>> listLiked(@PathVariable Long blogId, @RequestParam Long userId) {
        return Result.success(commentLikeService.getLikedCommentIds(blogId, userId));
    }

}
