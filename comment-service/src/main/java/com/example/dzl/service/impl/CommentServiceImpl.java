package com.example.dzl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.CommentErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.feign.AiServiceClient;
import com.example.dzl.feign.BlogServiceClient;
import com.example.dzl.mapper.CommentMapper;
import com.example.dzl.model.entity.Comment;
import com.example.dzl.model.req.CommentAddReq;
import com.example.dzl.model.req.CommentAdminReq;
import com.example.dzl.model.req.CommentPageReq;
import com.example.dzl.model.req.CommentReplyReq;
import com.example.dzl.model.vo.CommentVO;
import com.example.dzl.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private AiServiceClient aiServiceClient;

    @Autowired
    private BlogServiceClient blogServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CommentAddReq req) {
        Comment c = new Comment();
        c.setId(IdWorker.getId());
        c.setBlogId(req.getBlogId());
        c.setRootId(c.getId());
        c.setParentId(0L);
        c.setUserId(req.getUserId());
        // 校验用户名是否为空
        if (Objects.isNull(req.getNickname()) || req.getNickname().trim().isEmpty()) {
            throw new BusinessException(CommentErrorEnum.COMMENT_NICKNAME_EMPTY);
        }

        c.setNickname(req.getNickname());
        c.setUserAvatar(req.getUserAvatar());
        // 校验评论内容是否为空
        if (Objects.isNull(req.getContent()) || req.getContent().trim().isEmpty()) {
            throw new BusinessException(CommentErrorEnum.COMMENT_CONTENT_EMPTY);
        }

        // AI 敏感词检测
        checkSensitiveWord(req.getContent());

        c.setContent(req.getContent());
        c.setStatus(1);
        c.setLikeCount(0L);
        this.save(c);
    }

    /**
     * 调用AI进行敏感词检测，含敏感词则抛出异常
     * 若AI服务不可用，则降级放行不阻媞用户发表
     */
    private void checkSensitiveWord(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            Map<String, String> body = Map.of("content", content);
            var result = aiServiceClient.hasSensitiveWord(body);
            if (result != null && Boolean.TRUE.equals(result.getData())) {
                log.warn("敏感词检测不通过，内容: {}", content);
                throw new BusinessException(CommentErrorEnum.COMMENT_SENSITIVE_WORD);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // AI 服务异常时降级放行，不阻止用户发表评论
            log.error("敏感词检测服务异常，降级放行: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(CommentReplyReq req) {
        log.info("reply req: {}", req);
        // 校验回复的评论是否存在
        Comment parent = this.getById(req.getParentId());
        if (Objects.isNull(parent)) {
            log.error("parent comment not found, parentId: {}", req.getParentId());
            throw new BusinessException(CommentErrorEnum.COMMENT_PARENT_NOT_EXIST);
        }
        log.info("parent userId: {}, req userId: {}", parent.getUserId(), req.getUserId());
        // 检查是否回复自己的评论
        if (Objects.equals(parent.getUserId(), req.getUserId())) {
            log.warn("reply self error, userId: {}", req.getUserId());
            throw new BusinessException(CommentErrorEnum.COMMENT_REPLY_SELF_ERROR);
        }

        Comment c = new Comment();
        c.setId(IdWorker.getId());
        c.setBlogId(req.getBlogId());
        c.setRootId(req.getRootId());
        c.setParentId(req.getParentId());
        c.setUserId(req.getUserId());
        if (Objects.isNull(req.getNickname()) || req.getNickname().trim().isEmpty()) {
            throw new BusinessException(CommentErrorEnum.COMMENT_NICKNAME_EMPTY);
        }
        c.setNickname(req.getNickname());
        c.setUserAvatar(req.getUserAvatar());
        c.setContent(req.getContent());
        c.setStatus(1);
        c.setLikeCount(0L);
        this.save(c);
    }

    @Override
    public void deleteComment(Long id) {
        this.removeById(id);
    }

    @Override
    public Page<CommentVO> page(CommentPageReq req) {
        Page<Comment> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getBlogId, req.getBlogId())
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        Page<Comment> pageResult = this.page(pageParam, wrapper);
        List<Comment> list = pageResult.getRecords();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<CommentVO> voList = list.stream()
                .map(c -> toVO(c, fmt))
                .collect(Collectors.toList());

        Page<CommentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<CommentVO> pageAdmin(CommentAdminReq req) {
        Page<Comment> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getBlogId() != null, Comment::getBlogId, req.getBlogId())
                .eq(req.getStatus() != null, Comment::getStatus, req.getStatus())
                .like(StringUtils.hasText(req.getNickname()), Comment::getNickname, req.getNickname())
                .like(StringUtils.hasText(req.getContent()), Comment::getContent, req.getContent())
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> pageResult = this.page(pageParam, wrapper);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 批量获取博客标题
        List<Long> blogIds = pageResult.getRecords().stream()
                .map(Comment::getBlogId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> titleMap = blogIds.isEmpty() ? Collections.emptyMap() : blogServiceClient.getTitles(blogIds);

        List<CommentVO> voList = pageResult.getRecords().stream()
                .map(c -> {
                    CommentVO vo = toVO(c, fmt);
                    vo.setBlogTitle(titleMap.get(c.getBlogId()));
                    return vo;
                })
                .collect(Collectors.toList());

        Page<CommentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(Long[] ids, Integer status) {
        if (ids == null || ids.length == 0) {
            return;
        }
        for (Long id : ids) {
            Comment comment = new Comment();
            comment.setId(id);
            comment.setStatus(status);
            this.updateById(comment);
        }
    }

    private CommentVO toVO(Comment c, DateTimeFormatter fmt) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setBlogId(c.getBlogId());
        vo.setRootId(c.getRootId());
        vo.setParentId(c.getParentId());
        vo.setUserId(c.getUserId());
        vo.setNickname(c.getNickname());
        vo.setUserAvatar(c.getUserAvatar());
        vo.setContent(c.getContent());
        vo.setStatus(c.getStatus());
        vo.setLikeCount(c.getLikeCount());
        vo.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().format(fmt) : null);
        return vo;
    }
}
