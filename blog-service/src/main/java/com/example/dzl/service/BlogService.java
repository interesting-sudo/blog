package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.req.AddBlogReq;
import com.example.dzl.model.req.BlogAdminReq;
import com.example.dzl.model.req.BlogPageReq;
import com.example.dzl.model.req.UpdateBlogReq;
import com.example.dzl.model.vo.BlogAdminVO;
import com.example.dzl.model.vo.BlogUserStatsVO;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService extends IService<Blog> {

    /**
     * 发布博客
     * @param addBlogReq 博客信息
     * @param coverImage 封面图片（可选）
     */
    void publish(AddBlogReq addBlogReq, MultipartFile coverImage);

    /**
     * 编辑博客
     * @param updateBlogReq 博客信息
     * @param coverImage 封面图片（可选）
     */
    void edit(UpdateBlogReq updateBlogReq, MultipartFile coverImage);

    /**
     * 删除博客
     * @param id 博客ID
     */
    void delete(Long id);

    /**
     * 获取博客详情
     * @param id 博客ID
     * @param ip 访问IP
     * @param userId 当前访问用户ID（可选，用于排除作者本人访问计数）
     * @return 博客详情
     */
    BlogDetailDTO getBlogDetail(Long id, String ip, Long userId);

    /**
     * 分页查询博客列表
     * @param req 分页请求参数
     * @return 分页结果
     */
    Page<BlogDetailDTO> page(BlogPageReq req);

    /**
     * 将现有所有已发布博客同步到搜索服务（用于初始化 ES 索引）
     */
    void rebuildSearchIndex();

    /**
     * 获取用户博客统计数据
     * @param userId 用户ID
     * @return 统计数据
     */
    BlogUserStatsVO getUserStats(Long userId);

    /**
     * 批量更新博客状态
     * @param blogIds 博客ID数组
     * @param status 目标状态
     */
    void batchUpdateStatus(Long[] blogIds, Integer status);

    /**
     * 管理端：分页查询博客列表（包含所有状态及全字段搜索）
     * @param req 分页请求参数
     * @return 分页结果
     */
    Page<BlogAdminVO> pageAdmin(BlogAdminReq req);
}
