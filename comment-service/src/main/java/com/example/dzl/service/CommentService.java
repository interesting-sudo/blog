package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.entity.Comment;
import com.example.dzl.model.req.CommentAddReq;
import com.example.dzl.model.req.CommentAdminReq;
import com.example.dzl.model.req.CommentPageReq;
import com.example.dzl.model.req.CommentReplyReq;
import com.example.dzl.model.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    void add(CommentAddReq req);
    void reply(CommentReplyReq req);
    void deleteComment(Long id);
    Page<CommentVO> page(CommentPageReq req);

    /**
     * 管理端：分页查询评论列表
     * @param req 查询参数
     * @return 分页结果
     */
    Page<CommentVO> pageAdmin(CommentAdminReq req);

    /**
     * 批量更新评论状态（正常/禁用）
     * @param ids 评论ID列表
     * @param status 状态:0-禁用,1-启用
     */
    void batchUpdateStatus(Long[] ids, Integer status);
}
