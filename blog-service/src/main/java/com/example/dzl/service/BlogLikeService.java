package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.entity.BlogLike;

public interface BlogLikeService extends IService<BlogLike> {
    /**
     * 点赞或取消点赞
     * @param blogId 博客 ID
     * @param userId 用户 ID
     */
    boolean toggleLike(Long blogId, Long userId);

    /**
     * 点赞
     * @param blogId 博客 ID
     * @param userId 用户 ID
     */
    void like(Long blogId, Long userId);

    /**
     * 取消点赞
     * @param blogId 博客 ID
     * @param userId 用户 ID
     */
    void unlike(Long blogId, Long userId);

    /**
     * 判断是否点赞
     * @param blogId 博客 ID
     * @param userId 用户 ID
     */
    boolean isLiked(Long blogId, Long userId);

    /**
     * 获取博客的点赞数
     * @param blogId 博客 ID
     */
    long countLikes(Long blogId);


}
