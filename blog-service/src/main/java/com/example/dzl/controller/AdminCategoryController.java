package com.example.dzl.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.entity.Category;
import com.example.dzl.model.req.CategoryReq;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.CategoryAdminVO;
import com.example.dzl.service.CategoryService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：分类管理
 * 仅限管理员角色访问
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表（分页）
     */
    @PostMapping("/list")
    public Result<Page<CategoryAdminVO>> list(@RequestBody PageReq req) {
        log.info("管理员操作：获取分类列表, req: {}", req);
        return Result.success(categoryService.pageAdmin(req));
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result<Void> addCategory(@RequestBody CategoryReq req) {
        log.info("管理员操作：新增分类 {}", req.getName());
        Category category = new Category(req.getName(), req.getDescription());
        categoryService.save(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("管理员操作：删除分类 {}", categoryId);
        categoryService.removeById(categoryId);
        return Result.success();
    }

    /**
     * 修改分类信息
     */
    @PutMapping("/update")
    public Result<Void> updateCategory(@RequestBody CategoryReq req) {
        log.info("管理员操作：更新分类 {} 为 {}", req.getId(), req.getName());
        categoryService.updateCategoryWithSync(req.getId(), req.getName(), req.getDescription());
        return Result.success();
    }
}
