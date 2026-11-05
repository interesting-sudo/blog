package com.example.dzl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.BlogErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.mapper.BlogFavoriteMapper;
import com.example.dzl.mapstruct.BlogConvert;
import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.BlogFavorite;
import com.example.dzl.service.BlogFavoriteService;
import com.example.dzl.service.BlogService;
import com.example.dzl.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BlogFavoriteServiceImpl extends ServiceImpl<BlogFavoriteMapper, BlogFavorite> implements BlogFavoriteService {

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogConvert blogConvert;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public boolean toggleFavorite(Long blogId, Long userId) {
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogFavorite record = this.getOne(new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getBlogId, blogId)
                .eq(BlogFavorite::getUserId, userId));

        if (record == null) {
            record = new BlogFavorite(blogId, userId);
            this.save(record);
            // 更新博客收藏数
            blog.setFavoriteCount(blog.getFavoriteCount() + 1);
            blogService.updateById(blog);
            return true;
        }

        if (record.getStatus() == 1) {
            // 如果已收藏，则取消收藏
            record.setStatus(0);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            blog.setFavoriteCount(blog.getFavoriteCount() - 1);
            blogService.updateById(blog);
            return false;
        } else {
            // 如果未收藏，则收藏
            record.setStatus(1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            blog.setFavoriteCount(blog.getFavoriteCount() + 1);
            blogService.updateById(blog);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long blogId, Long userId) {
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogFavorite record = this.getOne(new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getBlogId, blogId)
                .eq(BlogFavorite::getUserId, userId));
        if (record == null) {
            record = new BlogFavorite(blogId, userId);
            this.save(record);
        } else if (record.getStatus() == 0) {
            record.setStatus(1);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            // 更新博客收藏数
            blog.setFavoriteCount(blog.getFavoriteCount() + 1);
            blogService.updateById(blog);
        } else {
            throw new BusinessException(BlogErrorEnum.BLOG_ALREADY_FAVORITE);
        }
        log.info("收藏成功");
    }

    @Override
    public void unFavorite(Long blogId, Long userId) {
        log.info("取消收藏");
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        BlogFavorite record = this.getOne(new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getBlogId, blogId)
                .eq(BlogFavorite::getUserId, userId));

        if (record == null || record.getStatus() == 0) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_FAVORITE);
        }
        record.setStatus(0);
        record.setUpdateTime(LocalDateTime.now());
        this.updateById(record);
        // 更新博客收藏数
        blog.setFavoriteCount(blog.getFavoriteCount() - 1);
        blogService.updateById(blog);
        log.info("取消收藏成功");
    }

    /**
     * 检查用户是否收藏了博客
     * @param blogId 博客ID
     * @param userId 用户ID
     * @return 是否收藏了博客
     */
    @Override
    public boolean isFavorited(Long blogId, Long userId) {
        return this.count(new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getBlogId, blogId)
                .eq(BlogFavorite::getUserId, userId)
                .eq(BlogFavorite::getStatus, 1)) > 0;
    }
    // 统计博客的收藏数
    @Override
    public long countFavorites(Long blogId) {
        return this.count(new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getBlogId, blogId)
                .eq(BlogFavorite::getStatus, 1));
    }


    @Override
    public Page<BlogDetailDTO> pageFavoriteBlogs(Long userId, int pageNum, int pageSize) {
        // 分页查询用户收藏的博客
        Page<BlogFavorite> favoritePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlogFavorite> wrapper = new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getUserId, userId)
                .eq(BlogFavorite::getStatus, 1)
                .orderByDesc(BlogFavorite::getCreateTime);

        Page<BlogFavorite> favoriteResult = this.page(favoritePage, wrapper);

        if (favoriteResult.getRecords() == null || favoriteResult.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        // 获取收藏的博客ID列表
        List<Long> blogIds = favoriteResult.getRecords().stream()
                .map(BlogFavorite::getBlogId)
                .toList();
        // 查询收藏的博客实体
        List<Blog> blogs = blogService.list(new LambdaQueryWrapper<Blog>()
                .in(Blog::getId, blogIds)
                .eq(Blog::getStatus, 1));

        if (blogs == null || blogs.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        // 将博客实体映射为ID到实体的映射
        Map<Long, Blog> blogMap = blogs.stream()
                .collect(Collectors.toMap(Blog::getId, b -> b));

        // 根据收藏记录的顺序对博客实体进行排序
        List<Blog> orderedBlogs = favoriteResult.getRecords().stream()
                .map(f -> blogMap.get(f.getBlogId()))
                .filter(Objects::nonNull)
                .toList();

        List<BlogDetailDTO> dtoList = blogConvert.entityListToDTOList(orderedBlogs);

        if (!dtoList.isEmpty()) {
            Set<Long> categoryIds = dtoList.stream()
                    .map(BlogDetailDTO::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!categoryIds.isEmpty()) {
                Map<Long, String> categoryMap = categoryService.getCategoryNamesByIds(categoryIds);
                dtoList.stream()
                        .filter(dto -> dto.getCategoryId() != null)
                        .forEach(dto -> dto.setCategoryName(categoryMap.get(dto.getCategoryId())));
            }
        }

        Page<BlogDetailDTO> resultPage = new Page<>(favoriteResult.getCurrent(), favoriteResult.getSize(), favoriteResult.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }


}

