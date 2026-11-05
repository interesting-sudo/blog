package com.example.dzl.feign;

import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.mq.UserProfileChangedEvent;
import com.example.dzl.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "search-service")
public interface SearchServiceClient {

    @PostMapping("/internal/search/blog/index")
    Result<Void> indexBlog(@RequestBody BlogDetailDTO dto);

    @DeleteMapping("/internal/search/blog/{id}")
    Result<Void> deleteBlog(@PathVariable("id") Long id);

    @PostMapping("/internal/search/author/profile/update")
    Result<Void> updateAuthorProfile(@RequestBody UserProfileChangedEvent event);

    /**
     * 更新分类名称
     */
    @PostMapping("/internal/search/category/name/update")
    Result<Void> updateCategoryName(@RequestBody java.util.Map<String, Object> request);

    /**
     * 更新标签名称
     */
    @PostMapping("/internal/search/tag/name/update")
    Result<Void> updateTagName(@RequestBody java.util.Map<String, String> request);
}
