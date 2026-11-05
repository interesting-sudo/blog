package com.example.dzl.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author 光的代言人
 */
@Data
@TableName("sys_blog")
public class Blog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 博客标题
     */
    private String title;

    /**
     * 博客内容（支持Markdown）
     */
    private String content;

    /**
     * 博客摘要/简介
     */
    private String summary;

    /**
     * 作者ID（关联用户表）
     */
    private Long authorId;

    /**
     * 作者昵称（冗余字段，提高查询效率）
     */
    private String authorName;

    /**
     * 作者头像URL（冗余字段，提高查询效率）
     */
    private String authorAvatar;

    /**
     * 分类ID（可选，关联分类表）
     */
    private Long categoryId;



    /**
     * 标签（多个标签用逗号分隔，如：Java,Spring,微服务）
     */
    private String tags;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 状态：0-草稿，1-上架，2-下架
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;
    /**
     * 收藏数
     */
    private Long favoriteCount;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
