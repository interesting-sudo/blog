package com.example.dzl.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.BlogErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.feign.SearchServiceClient;
import com.example.dzl.mapper.BlogMapper;
import com.example.dzl.mapper.CategoryMapper;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.Category;
import com.example.dzl.model.req.PageReq;
import com.example.dzl.model.vo.CategoryAdminVO;
import com.example.dzl.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    /**
     * 根据ID获取分类名称
     */
    @Override
    public String getCategoryNameById(Long categoryId) {
        if (categoryId == null){
            return null;
        }
        Category category = this.getById(categoryId);
        return category != null ? category.getName() : null;
    }
    /**
     * 根据ID列表获取分类名称
     */
    @Override
    public Map<Long, String> getCategoryNamesByIds(Collection<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        return this.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Override
    public Page<CategoryAdminVO> pageAdmin(PageReq req) {
        Page<Category> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        Page<Category> categoryPage = this.page(pageParam);

        List<CategoryAdminVO> adminVOList = categoryPage.getRecords().stream().map(category -> {
            CategoryAdminVO vo = new CategoryAdminVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setDescription(category.getDescription());
            vo.setCreateTime(category.getCreateTime());

            // 查询关联文章数
            Long count = blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                    .eq(Blog::getCategoryId, category.getId()));
            vo.setBlogCount(count);
            return vo;
        }).collect(Collectors.toList());

        Page<CategoryAdminVO> resultPage = new Page<>(categoryPage.getCurrent(), categoryPage.getSize(), categoryPage.getTotal());
        resultPage.setRecords(adminVOList);
        return resultPage;
    }

    @Override
    public boolean removeById(Serializable id) {
        // 检查是否有文章关联此分类
        Long count = blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException(BlogErrorEnum.CATEGORY_HAS_BLOGS);
        }
        return super.removeById(id);
    }

    @Override
    public void updateCategoryWithSync(Long id, String name, String description) {
        log.info("开始更新分类, id: {}, name: {}", id, name);
        
        // 获取旧分类名称，用于判断是否需要更新ES
        Category oldCategory = this.getById(id);
        String oldCategoryName = oldCategory != null ? oldCategory.getName() : null;
        
        // 更新数据库
        Category category = new Category(name, description);
        category.setId(id);
        this.updateById(category);
        
        // 如果分类名称发生变化，同步更新ES索引
        if (oldCategoryName != null && !oldCategoryName.equals(name)) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("categoryId", id);
                request.put("categoryName", name);
                searchServiceClient.updateCategoryName(request);
                log.info("同步更新ES分类名称成功, categoryId={}, oldName={}, newName={}", 
                        id, oldCategoryName, name);
            } catch (Exception e) {
                log.error("同步更新ES分类名称失败, categoryId={}", id, e);
                // 不抛出异常，避免影响主流程
            }
        }
        
        log.info("更新分类成功, id: {}", id);
    }
}
