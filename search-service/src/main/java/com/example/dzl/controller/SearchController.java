package com.example.dzl.controller;

import com.example.dzl.model.dto.BlogIndexDTO;
import com.example.dzl.model.mq.UserProfileChangedEvent;
import com.example.dzl.model.req.BlogSearchReq;
import com.example.dzl.model.vo.BlogSearchVO;
import com.example.dzl.model.vo.PageResult;
import com.example.dzl.service.SearchService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 索引博客
     * @param dto 博客索引DTO
     * @return 成功结果
     */
    @PostMapping("/internal/search/blog/index")
    public Result<Void> indexBlog(@RequestBody BlogIndexDTO dto) {
        searchService.indexBlog(dto);
        return Result.success();
    }

    /**
     * 删除博客索引
     * @param id 博客ID
     * @return 成功结果
     */
    @DeleteMapping("/internal/search/blog/{id}")
    public Result<Void> deleteBlog(@PathVariable Long id) {
        searchService.deleteBlog(id);
        return Result.success();
    }

    /**
     * 搜索博客
     * @param req 博客搜索请求
     * @return 博客搜索结果
     */
    @PostMapping("/search/blog")
    public Result<PageResult<BlogSearchVO>> search(@RequestBody BlogSearchReq req) {
        return Result.success(searchService.search(req));
    }
    /**
     * 更新作者个人信息
     * @param event 用户个人信息变更事件
     * @return 成功结果
     */
    @PostMapping("/internal/search/author/profile/update")
    public Result<Void> updateAuthorProfile(@RequestBody UserProfileChangedEvent event) {
        searchService.updateAuthorProfile(event.getUserId(), event.getNickname(), event.getAvatar());
        return Result.success();
    }

    /**
     * 更新分类名称
     * @param request 包含分类ID和新名称的请求体
     * @return 成功结果
     */
    @PostMapping("/internal/search/category/name/update")
    public Result<Void> updateCategoryName(@RequestBody java.util.Map<String, Object> request) {
        Long categoryId = ((Number) request.get("categoryId")).longValue();
        String categoryName = (String) request.get("categoryName");
        searchService.updateCategoryName(categoryId, categoryName);
        return Result.success();
    }

    /**
     * 更新标签名称
     * @param request 包含旧标签名和新标签名的请求体
     * @return 成功结果
     */
    @PostMapping("/internal/search/tag/name/update")
    public Result<Void> updateTagName(@RequestBody java.util.Map<String, String> request) {
        String oldTagName = request.get("oldTagName");
        String newTagName = request.get("newTagName");
        searchService.updateTagName(oldTagName, newTagName);
        return Result.success();
    }
}

