package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.entity.BlogFavorite;

public interface BlogFavoriteService extends IService<BlogFavorite> {
    /**
     * 切换收藏状态（如果已收藏则取消收藏，否则收藏）
     * @param blogId 博客ID
     * @param userId 用户ID
     * @return 是否成功切换收藏状态
     */
    boolean toggleFavorite(Long blogId, Long userId);

    /**
     * 收藏博客
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    void favorite(Long blogId, Long userId);

    /**
     * 取消收藏博客
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    void unFavorite(Long blogId, Long userId);

    /**
     * 检查用户是否收藏了博客
     * @param blogId 博客ID
     * @param userId 用户ID
     * @return 是否收藏了博客
     */
    boolean isFavorited(Long blogId, Long userId);

     /**
      * 统计博客的收藏数
      * @param blogId 博客ID
      * @return 收藏数
      */
    long countFavorites(Long blogId);

    /**
     * 分页查询用户收藏的博客列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<BlogDetailDTO> pageFavoriteBlogs(Long userId, int pageNum, int pageSize);
}
