package com.example.dzl.service;

import com.example.dzl.model.dto.BlogIndexDTO;
import com.example.dzl.model.req.BlogSearchReq;
import com.example.dzl.model.vo.BlogSearchVO;
import com.example.dzl.model.vo.PageResult;

public interface SearchService {
    /**
     * 索引博客
     * @param dto 博客索引DTO
     */
    void indexBlog(BlogIndexDTO dto);

    /**
     * 删除博客索引
     * @param id 博客ID
     */
    void deleteBlog(Long id);

    /**
     * 搜索博客
     * @param req 博客搜索请求
     * @return 博客搜索结果
     */
    PageResult<BlogSearchVO> search(BlogSearchReq req);

    /**
     * 更新作者资料
     * @param authorId 作者ID
     * @param authorName 作者名称
     * @param authorAvatar 作者头像
     */
    void updateAuthorProfile(Long authorId, String authorName, String authorAvatar);

    /**
     * 批量更新分类名称
     * @param categoryId 分类ID
     * @param categoryName 新的分类名称
     */
    void updateCategoryName(Long categoryId, String categoryName);

    /**
     * 批量更新标签名称
     * @param oldTagName 旧标签名称
     * @param newTagName 新标签名称
     */
    void updateTagName(String oldTagName, String newTagName);
}

