package com.example.dzl.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.req.CommentAdminReq;
import com.example.dzl.model.vo.CommentVO;
import com.example.dzl.service.CommentService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：评论管理
 * 仅限管理员角色访问
 */
@RestController
@RequestMapping("/admin/comment")
@Slf4j
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论列表（管理端：支持全状态、全字段搜索）
     * @param req 查询参数
     */
    @PostMapping("/list")
    public Result<Page<CommentVO>> listAdmin(@RequestBody CommentAdminReq req) {
        log.info("管理端获取评论列表请求: {}", req);
        return Result.success(commentService.pageAdmin(req));
    }

    /**
     * 批量更新评论状态（正常/禁用）
     * @param ids 评论ID列表
     * @param status 状态:0-禁用,1-启用
     */
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody Long[] ids, @RequestParam Integer status) {
        log.info("管理员操作：批量更新评论 {} 状态为 {}", (Object) ids, status);
        commentService.batchUpdateStatus(ids, status);
        return Result.success();
    }
}
