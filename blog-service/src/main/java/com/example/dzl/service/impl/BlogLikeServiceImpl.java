package com.example.dzl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.BlogErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.mapper.BlogLikeMapper;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.BlogLike;
import com.example.dzl.service.BlogLikeService;
import com.example.dzl.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Slf4j
public class BlogLikeServiceImpl extends ServiceImpl<BlogLikeMapper, BlogLike> implements BlogLikeService {

    @Autowired
    private BlogService blogService;

    @Override
    @Transactional
    public boolean toggleLike(Long blogId, Long userId) {
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogLike record = this.getOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId));

        if (record == null) {
            // 新增
            record = new BlogLike(blogId, userId);
            this.save(record);
            blog.setLikeCount(blog.getLikeCount() + 1);
            blogService.updateById(blog);
            return true;
        }

        if (record.getLikeStatus() == 1) {
            // 取消点赞
            record.setLikeStatus(0);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            blog.setLikeCount(blog.getLikeCount() - 1);
            blogService.updateById(blog);
            return false;
        } else {
            // 点赞
            record.setLikeStatus(1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            blog.setLikeCount(blog.getLikeCount() + 1);
            blogService.updateById(blog);
            return true;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long blogId, Long userId) {
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogLike record = this.getOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId));
        // 判断是否已经点赞
        if (record == null) {
            // 新增
            record = new BlogLike(blogId, userId);
            this.save(record);
            blog.setLikeCount(blog.getLikeCount() + 1);
        } else if (record.getLikeStatus() == 0) {
            // 点赞
            record.setLikeStatus(1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            blog.setLikeCount(blog.getLikeCount() + 1);
        }else{
            throw new BusinessException(BlogErrorEnum.BLOG_ALREADY_LIKE);
        }
        // 更新博客点赞数
        blogService.updateById(blog);
        log.info("点赞成功");
    }

    @Override
    public void unlike(Long blogId, Long userId) {
        log.info("取消点赞");
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogLike record = this.getOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId));

        if (record == null||record.getLikeStatus() == 0) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_LIKE);
        }
        record.setLikeStatus(0);
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);
        blog.setLikeCount(blog.getLikeCount() - 1);
        blogService.updateById(blog);
        log.info("取消点赞成功");
    }

    @Override
    public boolean isLiked(Long blogId, Long userId) {
        // 判断是否已经点赞
        return this.count(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId)
                .eq(BlogLike::getLikeStatus, 1))>0;
    }

    @Override
    public long countLikes(Long blogId) {
        return this.count((new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getLikeStatus, 1)));
    }
}
