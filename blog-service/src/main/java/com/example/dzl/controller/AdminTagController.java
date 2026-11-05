package com.example.dzl.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.entity.Tag;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.req.TagReq;
import com.example.dzl.model.vo.TagAdminVO;
import com.example.dzl.service.TagService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：标签管理
 * 仅限管理员角色访问
 */
@RestController
@RequestMapping("/admin/tag")
@Slf4j
public class AdminTagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取标签列表（分页）
     */
    @PostMapping("/list")
    public Result<Page<TagAdminVO>> list(@RequestBody PageReq req) {
        log.info("管理员操作：获取标签列表, req: {}", req);
        return Result.success(tagService.pageAdmin(req));
    }
    /**
     * 新增标签
     */
    @PostMapping("/add")
    public Result<Void> addTag(@RequestBody TagReq req) {
        log.info("管理员操作：新增标签 {}", req.getName());
        Tag tag = new Tag();
        tag.setName(req.getName());
        tagService.save(tag);
        return Result.success();
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        log.info("管理员操作：删除标签 {}", tagId);
        tagService.removeById(tagId);
        return Result.success();
    }

    /**
     * 修改标签信息
     */
    @PutMapping("/update")
    public Result<Void> updateTag(@RequestBody TagReq req) {
        log.info("管理员操作：更新标签 {} 为 {}", req.getId(), req.getName());
        tagService.updateTagWithSync(req.getId(), req.getName());
        return Result.success();
    }
}
