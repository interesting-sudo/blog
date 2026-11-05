package com.example.dzl.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.BlogErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.feign.DocumentServiceClient;
import com.example.dzl.feign.SearchServiceClient;
import com.example.dzl.feign.UserServiceClient;
import com.example.dzl.mapper.BlogMapper;
import com.example.dzl.mapper.BlogTagMapper;
import com.example.dzl.mapstruct.BlogConvert;
import com.example.dzl.model.dto.AddBlogDTO;
import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.dto.UpdateBlogDTO;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.BlogTag;
import com.example.dzl.model.req.AddBlogReq;
import com.example.dzl.model.req.BlogAdminReq;
import com.example.dzl.model.req.BlogPageReq;
import com.example.dzl.model.req.UpdateBlogReq;
import com.example.dzl.model.vo.BlogAdminVO;
import com.example.dzl.model.vo.BlogUserStatsVO;
import com.example.dzl.service.BlogService;
import com.example.dzl.service.CategoryService;
import com.example.dzl.service.TagService;
import com.example.dzl.utils.Result;
import com.example.dzl.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DocumentServiceClient documentServiceClient;
    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BlogConvert blogConvert;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(AddBlogReq addBlogReq, MultipartFile coverImage) {
        log.info("处理博客发布：{}", addBlogReq);
        AddBlogDTO addBlogDTO = blogConvert.reqToDTO(addBlogReq);
        log.info("AddBlogDTO: {}", addBlogDTO);
        Long authorId = addBlogDTO.getAuthorId();
        // 获取用户信息(通过feign调用)
        UserInfoVO userInfoVO = (UserInfoVO) userServiceClient.getUserInfo(1L).getData();
        if (userInfoVO == null) {
            throw new BusinessException(BlogErrorEnum.USER_NOT_EXIST);
        }
        log.info("UserInfoVO: {}", userInfoVO);
        // 设置作者信息
        addBlogDTO.setAuthorName(userInfoVO.getNickname());
        addBlogDTO.setAuthorAvatar(userInfoVO.getAvatarUrl());

        // 3. 处理封面图片上传（如果提供了封面图片文件）
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                Result<String> uploadResult = documentServiceClient.uploadImage(coverImage);
                if (uploadResult != null && uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                    String coverImageUrl = uploadResult.getData();
                    addBlogDTO.setCoverImage(coverImageUrl);
                    log.info("封面图片上传成功: URL={}", coverImageUrl);
                } else {
                    log.warn("封面图片上传失败: {}", uploadResult != null ? uploadResult.getMsg() : "未知错误");
                    throw new RuntimeException("封面图片上传失败");
                }
            } catch (Exception e) {
                log.error("上传封面图片时发生异常", e);
                throw new RuntimeException("封面图片上传失败: " + e.getMessage(), e);
            }
        }
        // 验证分类是否存在（可去掉）
        log.info("处理分类信息：{}", addBlogDTO.getCategoryId());
        if(addBlogDTO.getCategoryId() != null){
            String categoryName = categoryService.getCategoryNameById(addBlogDTO.getCategoryId());
            if (categoryName == null){
                throw new BusinessException(BlogErrorEnum.CATEGORY_NOT_EXIST);
            }
        }
        // 设置默认状态（如果未指定）
        if (addBlogDTO.getStatus() == null) {
            // 默认启用
            addBlogDTO.setStatus(1);
        }
        // 发布博客
        Blog blog = blogConvert.dtoToEntity(addBlogDTO);
        blog.setViewCount(0L);
        blog.setLikeCount(0L);
        this.save(blog);
        log.info("发布博客成功：{}", blog);

        // TODO: 同步博客到ES
        BlogDetailDTO blogDetailDTO = blogConvert.entityToDTO(blog);
        if(blog.getCategoryId() != null){
            blogDetailDTO.setCategoryName(categoryService.getCategoryNameById(blog.getCategoryId()));
        }
        blogDetailDTO.setStatus(blog.getStatus());
//        try {
//            searchServiceClient.indexBlog(blogDetailDTO);
//        } catch (Exception e) {
//            log.error("同步博客到ES时发生异常，id={}",blog.getId(), e);
//        }
        // 索引博客，添加事务同步
        indexAfterCommit(blogDetailDTO);

        // 处理标签关联
        String tags = addBlogDTO.getTags();
        handleTags(blog.getId(), tags);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(UpdateBlogReq updateBlogReq, MultipartFile coverImage) {
        UpdateBlogDTO updateBlogDTO = blogConvert.reqToDTO(updateBlogReq);
        Blog blog = this.getById(updateBlogDTO.getId());
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        // 处理封面图片
        if (coverImage != null && !coverImage.isEmpty()) {
            Result<String> uploadResult = documentServiceClient.uploadImage(coverImage);
            if (uploadResult != null && uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                String coverImageUrl = uploadResult.getData();
                updateBlogDTO.setCoverImage(coverImageUrl);
            }
        } else if (StringUtils.hasText(updateBlogReq.getCoverImage())) {
            // 如果没有提供新的封面图片，则使用原始的封面图片
            updateBlogDTO.setCoverImage(updateBlogReq.getCoverImage());
        }

        // 验证分类是否存在
        if(updateBlogDTO.getCategoryId() != null){
            String categoryName = categoryService.getCategoryNameById(updateBlogDTO.getCategoryId());
            if (categoryName == null){
                throw new BusinessException(BlogErrorEnum.CATEGORY_NOT_EXIST);
            }
        }



        // 设置默认状态（如果未指定）
        if (updateBlogDTO.getStatus() == null) {
            // 默认启用
            updateBlogDTO.setStatus(1);
        }

        blog = blogConvert.dtoToEntity(updateBlogDTO);
        this.updateById(blog);
        log.info("更新博客成功：{}", blog);

        // TODO: 更新博客到ES
        BlogDetailDTO blogDetailDTO = blogConvert.entityToDTO(blog);
        if(blog.getCategoryId() != null){
            blogDetailDTO.setCategoryName(categoryService.getCategoryNameById(blog.getCategoryId()));
        }
        blogDetailDTO.setStatus(blog.getStatus());
//        try {
//            searchServiceClient.indexBlog(blogDetailDTO);
//        } catch (Exception e){
//            log.error("更新博客到ES时发生异常，id={}",blog.getId(), e);
//        }
        indexAfterCommit(blogDetailDTO);


        // 处理标签关联
        String tags = updateBlogDTO.getTags();
        if (tags != null && !tags.isEmpty()) {
            // 删除旧的标签关联关系
            blogTagMapper.delete(new LambdaQueryWrapper<BlogTag>().eq(BlogTag::getBlogId, blog.getId()));
            // 创建新的标签关联关系
            handleTags(blog.getId(), tags);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Blog blog = this.getById(id);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        // 删除博客
        this.removeById(id);
        // 删除标签关联
        blogTagMapper.delete(new LambdaQueryWrapper<BlogTag>().eq(BlogTag::getBlogId, id));
        log.info("删除博客成功：{}", blog);
        // TODO: 删除ES中的数据
//        try {
//            searchServiceClient.deleteBlog(id);
//        } catch (Exception e) {
//            log.error("删除搜索索引时发生异常，id={}",id, e);
//        }
        deleteAfterCommit(id);
    }

    @Override
    public BlogDetailDTO getBlogDetail(Long id, String ip, Long userId) {

        Blog blog = this.getById(id);
        if (blog == null) {
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_EXIST);
        }
        // 判断博客是否已发布，多加一个判断，作者本人可以查看
        if(blog.getStatus() != 1 && !blog.getAuthorId().equals(userId)){
            throw new BusinessException(BlogErrorEnum.BLOG_NOT_PUBLISHED);
        }
        // 浏览量更新，弹药排除作者本人，且同一IP/用户1小时内不重复计数
        boolean isAuthor = userId != null && userId.equals(blog.getAuthorId());
        if(!isAuthor){
            // 构造防重 Key：优先使用 userId,其次使用IP
            String uniqueId = (userId != null) ? "user_" + userId : "ip_" + ip;
            String viewKey = "blog:view:" + id+":"+uniqueId;
            // 尝试设置 key ，如果不存在则设置成功 返回为 true，过期时间 1 小时
            Boolean isNewView = stringRedisTemplate.opsForValue().setIfAbsent(viewKey, "1", 1, TimeUnit.HOURS);
            // 如果是新的浏览量，增加博客的浏览量
            if(Boolean.TRUE.equals(isNewView)){
                this.update(new LambdaUpdateWrapper<Blog>()
                        .eq(Blog::getId, id)
                        .setSql("view_count = view_count + 1"));
                // 更新内存中对象的浏览量，以便返回最新数据
                blog.setViewCount(blog.getViewCount() + 1);
            }
        }
        BlogDetailDTO blogDetailDTO = blogConvert.entityToDTO(blog);
        // 填充分类名称
        if (blog.getCategoryId() != null) {
            blogDetailDTO.setCategoryName(categoryService.getCategoryNameById(blog.getCategoryId()));
        }


        return blogDetailDTO;
    }

    @Override
    public Page<BlogDetailDTO> page(BlogPageReq req) {
        Page<Blog> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();

        // 查询条件
        if (req.getAuthorId() != null) {
            wrapper.eq(Blog::getAuthorId, req.getAuthorId());
        }
        if (req.getCategoryId() != null) {
            wrapper.eq(Blog::getCategoryId, req.getCategoryId());
        }
        // 状态过滤：onlyPublished为true时只查询已发布
        if (Boolean.TRUE.equals(req.getOnlyPublished())) {
            wrapper.eq(Blog::getStatus, 1);
        }

        wrapper.orderByDesc(Blog::getCreateTime);

        Page<Blog> pageResult = this.page(pageParam, wrapper);

        // 转换结果
        List<BlogDetailDTO> dtoList = blogConvert.entityListToDTOList(pageResult.getRecords());

        // 批量填充分类名称
        fillCategoryNames(dtoList);

        Page<BlogDetailDTO> resultPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public BlogUserStatsVO getUserStats(Long userId) {
        QueryWrapper<Blog> wrapper = new QueryWrapper<>();
        // 查询用户发布的博客数量、总点赞数、总浏览数，用户收藏的博客数
        wrapper.select("count(*) as blog_count", "ifnull(sum(like_count), 0) as total_likes", "ifnull(sum(view_count), 0) as total_views", "ifnull(sum(favorite_count), 0) as total_favorites")
                .eq("author_id", userId);

        Map<String, Object> map = this.getMap(wrapper);

        BlogUserStatsVO vo = new BlogUserStatsVO();
        if (map != null) {
            vo.setBlogCount(parseLong(map.get("blog_count")));
            vo.setTotalLikes(parseLong(map.get("total_likes")));
            vo.setTotalViews(parseLong(map.get("total_views")));
            vo.setTotalFavorites(parseLong(map.get("total_favorites")));
        } else {
            vo.setBlogCount(0L);
            vo.setTotalLikes(0L);
            vo.setTotalViews(0L);
            vo.setTotalFavorites(0L);
        }
        return vo;
    }

    /**
     * 解析对象为 Long 类型，处理 null 值和 Number 类型
     * @param obj 要解析的对象
     * @return 解析后的 Long 值，若解析失败则返回 0L
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildSearchIndex() {
        List<Blog> blogs = this.list(new LambdaQueryWrapper<Blog>().eq(Blog::getStatus, 1));
        log.info("开始重建搜索索引，{}", blogs);
        if (blogs == null || blogs.isEmpty()) {
            return;
        }
        List<BlogDetailDTO> dtoList = blogConvert.entityListToDTOList(blogs);
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
        // TODO: 批量查询博客详情时，状态要加上(后续可以去掉这一块，因为这是为了补充)
        Map<Long, Integer> statusMap = blogs.stream().collect(Collectors.toMap(Blog::getId, Blog::getStatus));
        dtoList.forEach(dto -> dto.setStatus(statusMap.get(dto.getId())));

        dtoList.forEach(detailDTO -> {
            try {
                searchServiceClient.indexBlog(detailDTO);
            } catch (Exception e) {
                log.error("重建搜索索引失败, id={}", detailDTO.getId(), e);
            }
        });
    }
    /**
     * 处理标签关联关系
     * @param blogId 博客ID
     * @param tags 标签列表
     */
    private void handleTags(Long blogId, String tags) {
        if(StringUtils.hasText( tags)){
            List<Long> tagIds = tagService.getOrCreateTagIds(tags);
            if (!tagIds.isEmpty()){
                List<BlogTag> blogTags = tagIds.stream()
                        .map(tagId -> new BlogTag(blogId, tagId))
                        .toList();
                // 批量插入博客标签关联关系
                blogTags.forEach(blogTag -> {
                    blogTagMapper.insert(blogTag);
                });
                log.info("处理标签关联关系成功：{}", blogTags);
            }
        }
    }

    @Override
    public Page<BlogAdminVO> pageAdmin(BlogAdminReq req) {
        Page<Blog> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();

        // 1. 基础过滤条件

        if (req.getStatus() != null) {
            wrapper.eq(Blog::getStatus, req.getStatus());
        }

        // 2. 模糊搜索条件
        if (StringUtils.hasText(req.getTitle())) {
            wrapper.like(Blog::getTitle, req.getTitle());
        }
        if (StringUtils.hasText(req.getAuthorName())) {
            wrapper.like(Blog::getAuthorName, req.getAuthorName());
        }

        if (StringUtils.hasText(req.getTags())) {
            wrapper.like(Blog::getTags, req.getTags());
        }

        wrapper.orderByDesc(Blog::getCreateTime);

        Page<Blog> pageResult = this.page(pageParam, wrapper);

        // 3. 转换结果并填充分类名称
        List<BlogAdminVO> voList = blogConvert.entityListToAdminVOList(pageResult.getRecords());

        // 批量填充分类名称
        if (voList != null && !voList.isEmpty()) {
            Set<Long> categoryIds = pageResult.getRecords().stream()
                    .map(Blog::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!categoryIds.isEmpty()) {
                Map<Long, String> categoryMap = categoryService.getCategoryNamesByIds(categoryIds);
                // 建立 ID 与名称的映射，并设置到 VO 中
                Map<Long, String> blogIdToCategoryNameMap = pageResult.getRecords().stream()
                        .filter(blog -> blog.getCategoryId() != null)
                        .collect(Collectors.toMap(Blog::getId, blog -> categoryMap.getOrDefault(blog.getCategoryId(), "")));

                voList.forEach(vo -> vo.setCategoryName(blogIdToCategoryNameMap.get(vo.getId())));
            }
        }

        Page<BlogAdminVO> resultPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 批量填充分类名称
     */
    private void fillCategoryNames(List<BlogDetailDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
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


    /**
     * 批量处理博客文章
     * @param blogIds 博客ID数组
     * @param status 目标状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(Long[] blogIds, Integer status) {
        if (blogIds == null || blogIds.length == 0) {
            return;
        }

        List<Blog> blogList = this.listByIds(Arrays.asList(blogIds));
        if (blogList.isEmpty()) {
            return;
        }

        blogList.forEach(blog -> blog.setStatus(status));
        this.updateBatchById(blogList);

        // 同步更新 ES
        blogList.forEach(blog -> {
            BlogDetailDTO blogDetailDTO = blogConvert.entityToDTO(blog);
            if (blog.getCategoryId() != null) {
                blogDetailDTO.setCategoryName(categoryService.getCategoryNameById(blog.getCategoryId()));
            }
            blogDetailDTO.setStatus(blog.getStatus());
            indexAfterCommit(blogDetailDTO);
        });
    }
    /**
     * 提交事务后，将博客索引加入索引队列
     * @param dto 博客详情
     */
    private void indexAfterCommit(BlogDetailDTO dto) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                int attempts = 0;
                // 重试3次
                while (attempts < 3) {
                    attempts++;
                    try {
                        log.info("重试第{}次:",attempts);
                        searchServiceClient.indexBlog(dto);
                        break;
                    } catch (Exception e) {
                        try {
                            // 重试第attempts次失败，则休眠200毫秒，并继续重试
                            log.info("重试第{}次,索引博客失败，id={}",attempts,dto.getId(), e);
                            Thread.sleep(200L);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        });
    }

    private void deleteAfterCommit(Long id) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                int attempts = 0;
                while (attempts < 3) {
                    attempts++;
                    try {
                        log.info("重试第{}次:",attempts);
                        searchServiceClient.deleteBlog(id);
                        break;
                    } catch (Exception e) {
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        });
    }
}
