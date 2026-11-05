package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.entity.Tag;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.TagAdminVO;

import java.util.List;

public interface TagService extends IService<Tag> {
    /**
     * 根据标签名称列表获取或创建标签ID列表
     * @param tagNames 标签名称列表（逗号分隔或列表）
     * @return 标签ID列表
     */
    List<Long> getOrCreateTagIds(String tagNames);

    /**
     * 分页查询标签列表（管理端）
     */
    Page<TagAdminVO> pageAdmin(PageReq req);

    /**
     * 更新标签信息（同步更新ES索引）
     * @param id 标签ID
     * @param name 标签名称
     */
    void updateTagWithSync(Long id, String name);
}
