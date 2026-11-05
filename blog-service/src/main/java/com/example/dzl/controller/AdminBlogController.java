package com.example.dzl.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.req.BlogAdminReq;
import com.example.dzl.model.vo.BlogAdminVO;
import com.example.dzl.service.BlogService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：博客管理
 * 仅限管理员角色访问
 */
@RestController
@RequestMapping("/admin/blog")
@Slf4j
public class AdminBlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 获取博客列表（管理端：支持全状态、全字段搜索）
     * @param req 查询参数
     */
    @PostMapping("/list")
    public Result<Page<BlogAdminVO>> listAdmin(@RequestBody BlogAdminReq req) {
        log.info("管理端获取博客列表请求: {}", req);
        return Result.success(blogService.pageAdmin(req));
    }

    /**
     * 批量下架/上架博客
     */
    @PutMapping("/batch/hide")
    public Result<Void> batchHide(@RequestBody Long[] blogIds,Integer status) {
        log.info("管理员操作：批量下架博客 {}", (Object) blogIds);
        blogService.batchUpdateStatus(blogIds,status);
        return Result.success();
    }
}
