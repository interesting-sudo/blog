package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.entity.Category;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.CategoryAdminVO;

import java.util.Collection;
import java.util.Map;

public interface CategoryService extends IService<Category> {
    /**
     * 根据ID获取分类名称
     */
    String getCategoryNameById(Long categoryId);

    /**
     * 批量根据ID获取分类名称
     * @param categoryIds 分类ID集合
     * @return Map<ID, Name>
     */
    Map<Long, String> getCategoryNamesByIds(Collection<Long> categoryIds);

    /**
     * 分页查询分类列表（管理端）
     */
    Page<CategoryAdminVO> pageAdmin(PageReq req);

    /**
     * 更新分类信息（同步更新ES索引）
     * @param id 分类ID
     * @param name 分类名称
     * @param description 分类描述
     */
    void updateCategoryWithSync(Long id, String name, String description);
}
