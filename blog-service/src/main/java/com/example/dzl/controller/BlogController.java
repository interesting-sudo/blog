package com.example.dzl.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.mapstruct.BlogConvert;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.req.AddBlogReq;
import com.example.dzl.model.req.BlogPageReq;
import com.example.dzl.model.req.UpdateBlogReq;
import com.example.dzl.model.vo.BlogDetailVO;
import com.example.dzl.model.vo.BlogUserStatsVO;
import com.example.dzl.service.BlogFavoriteService;
import com.example.dzl.service.BlogLikeService;
import com.example.dzl.service.BlogService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/blog")
@Slf4j
public class BlogController {

    @Autowired
    private BlogLikeService blogLikeService;

    @Autowired
    private BlogFavoriteService blogFavoriteService;

    @Autowired
    private BlogConvert blogConvert;

    @Autowired
    private BlogService blogService;

    /**
     * 发布博客
     * @param addBlogReq 博客信息（JSON格式，通过 @RequestPart 接收）
     * @param coverImage 封面图片（可选）
     */
    @PostMapping(value = "/publish", consumes = "multipart/form-data")
    public Result<Void> publish(
            @RequestPart("blog") AddBlogReq addBlogReq,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        log.info("发布博客请求: title={}, coverImage={}",
                addBlogReq.getTitle(), coverImage != null ? coverImage.getOriginalFilename() : "无");
        blogService.publish(addBlogReq, coverImage);
        return Result.success();
    }

    /**
     * 编辑博客
     * @param updateBlogReq 博客信息（JSON格式，通过 @RequestPart 接收）
     * @param coverImage 封面图片（可选）
     */
    @PutMapping(value = "/edit", consumes = "multipart/form-data")
    public Result<Void> edit(
            @RequestPart("blog") UpdateBlogReq updateBlogReq,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        log.info("编辑博客请求: id={}, title={}, coverImage={}", updateBlogReq.getId(), updateBlogReq.getTitle(),
                coverImage != null ? coverImage.getOriginalFilename() : "无");
        blogService.edit(updateBlogReq, coverImage);
        return Result.success();
    }

    /**
     * 删除博客
     * @param id 博客ID
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除博客请求: id={}", id);
        blogService.delete(id);
        return Result.success();
    }

    /**
     * 获取博客详情
     * @param id 博客ID
     * @param userId 当前用户ID（可选）
     */
    @GetMapping("/detail/{id}")
    public Result<BlogDetailVO> getBlogDetail(@PathVariable Long id,
                                              @RequestParam(required = false) Long userId,
                                              HttpServletRequest request) {
        log.info("获取博客详情请求: id={}, userId={}", id, userId);
        String ip = getIpAddress(request);
        return Result.success(blogConvert.dtoToVO(blogService.getBlogDetail(id, ip, userId)));
    }

    /**
     * 根据ID获取博客标题（内部接口）
     */
    @GetMapping("/internal/title/{id}")
    public String getTitle(@PathVariable Long id) {
        Blog blog = blogService.getById(id);
        return blog != null ? blog.getTitle() : null;
    }

    /**
     * 批量根据ID获取博客标题（内部接口）
     */
    @PostMapping("/internal/titles")
    public java.util.Map<Long, String> getTitles(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return blogService.listByIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Blog::getId, Blog::getTitle));
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 获取博客列表（只查询已发布的文章）
     * @param req 查询参数
     */
    @PostMapping("/list")
    public Result<Page<BlogDetailVO>> list(@RequestBody BlogPageReq req) {
        log.info("获取博客列表请求: {}", req);
        // 只查询已发布的文章
        req.setOnlyPublished(true);
        return Result.success(blogConvert.dtoPageToVOPage(blogService.page(req)));
    }

    /**
     * 获取指定用户的文章列表（包含该用户的所有文章，包括未发布的）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    @GetMapping("/list/user/{userId}")
    public Result<Page<BlogDetailVO>> listByUserId(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        log.info("获取用户文章列表请求: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        BlogPageReq req = new BlogPageReq();
        req.setAuthorId(userId);
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);
        // 查询该用户的所有文章，包括未发布的（onlyPublished为null表示不限制状态）
        req.setOnlyPublished(null);
        return Result.success(blogConvert.dtoPageToVOPage(blogService.page(req)));
    }

    /**
     * 获取指定用户收藏的文章列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    @GetMapping("/list/user/{userId}/favorites")
    public Result<Page<BlogDetailVO>> listFavoritesByUserId(@PathVariable Long userId,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("获取用户收藏文章列表请求: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        return Result.success(blogConvert.dtoPageToVOPage(blogFavoriteService.pageFavoriteBlogs(userId, pageNum, pageSize)));
    }

    /**
     * 获取指定用户的文章统计数据（文章数、总点赞数、总浏览数）
     * @param userId 用户ID
     */
    @GetMapping("/stats/user/{userId}")
    public Result<BlogUserStatsVO> getUserStats(@PathVariable Long userId) {
        log.info("获取用户文章统计数据请求: userId={}", userId);
        return Result.success(blogService.getUserStats(userId));
    }

    /**
     * 一次性将现有所有已发布博客同步到搜索服务
     */
    @PostMapping("/rebuildSearchIndex")
    public Result<Void> rebuildSearchIndex() {
        log.info("开始重建博客搜索索引");
        blogService.rebuildSearchIndex();
        return Result.success();
    }

    /**
     * 点赞切换
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/toggleLike")
    public Result<java.util.Map<String, Object>> toggleLike(@PathVariable Long blogId, @RequestParam Long userId) {
        boolean isLiked = blogLikeService.toggleLike(blogId, userId);
        Long likeCount = blogService.getById(blogId).getLikeCount();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("isLiked", isLiked);
        data.put("likeCount", likeCount);
        return Result.success(data);
    }

    /**
     * 点赞
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/like")
    public Result<Void> like(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("点赞请求: blogId={}, userId={}", blogId, userId);
        blogLikeService.like(blogId, userId);
        return Result.success();
    }

    /**
     * 取消点赞
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/unlike")
    public Result<Void> unlike(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("取消点赞请求: blogId={}, userId={}", blogId, userId);
        blogLikeService.unlike(blogId, userId);
        return Result.success();
    }



    /**
     * 查询是否点赞
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @GetMapping("/{blogId}/isLiked")
    public Result<Boolean> isLiked(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("查询是否点赞请求: blogId={}, userId={}", blogId, userId);
        return Result.success(blogLikeService.isLiked(blogId, userId));
    }

    /**
     * 查询点赞数
     * @param blogId 博客ID
     */
    @GetMapping("/{blogId}/likeCount")
    public Result<Long> likeCount(@PathVariable Long blogId) {
        log.info("查询点赞数请求: blogId={}", blogId);
        return Result.success(blogLikeService.countLikes(blogId));
    }

    /**
     * 收藏切换
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/toggleFavorite")
    public Result<java.util.Map<String, Object>> toggleFavorite(@PathVariable Long blogId, @RequestParam Long userId) {
        boolean isFavorited = blogFavoriteService.toggleFavorite(blogId, userId);
        long favoriteCount = blogFavoriteService.countFavorites(blogId);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("isFavorited", isFavorited);
        data.put("favoriteCount", favoriteCount);
        return Result.success(data);
    }

    /**
     * 收藏
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/favorite")
    public Result<Void> favorite(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("收藏请求: blogId={}, userId={}", blogId, userId);
        blogFavoriteService.favorite(blogId, userId);
        return Result.success();
    }

    /**
     * 取消收藏
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @PostMapping("/{blogId}/unfavorite")
    public Result<Void> unfavorite(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("取消收藏请求: blogId={}, userId={}", blogId, userId);
        blogFavoriteService.unFavorite(blogId, userId);
        return Result.success();
    }

    /**
     * 查询是否收藏
     * @param blogId 博客ID
     * @param userId 用户ID
     */
    @GetMapping("/{blogId}/isFavorited")
    public Result<Boolean> isFavorited(@PathVariable Long blogId, @RequestParam Long userId) {
        log.info("查询是否收藏请求: blogId={}, userId={}", blogId, userId);
        return Result.success(blogFavoriteService.isFavorited(blogId, userId));
    }

    /**
     * 查询收藏数
     * @param blogId 博客ID
     */
    @GetMapping("/{blogId}/favoriteCount")
    public Result<Long> favoriteCount(@PathVariable Long blogId) {
        log.info("查询收藏数请求: blogId={}", blogId);
        return Result.success(blogFavoriteService.countFavorites(blogId));
    }

}
