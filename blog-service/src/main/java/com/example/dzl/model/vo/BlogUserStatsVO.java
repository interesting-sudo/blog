package com.example.dzl.model.vo;

import lombok.Data;

@Data
public class BlogUserStatsVO {
    /**
     * 文章总数
     */
    private Long blogCount;

    /**
     * 文章总点赞数
     */
    private Long totalLikes;

    /**
     * 文章总浏览数
     */
    private Long totalViews;

    /**
     * 用户收藏的文章总数
     */
    private Long totalFavorites;
}
