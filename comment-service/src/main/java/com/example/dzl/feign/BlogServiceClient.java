package com.example.dzl.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "blog-service")
public interface BlogServiceClient {

    /**
     * 根据ID获取博客标题
     */
    @GetMapping("/blog/internal/title/{id}")
    String getTitle(@PathVariable("id") Long id);

    /**
     * 批量根据ID获取博客标题
     */
    @PostMapping("/blog/internal/titles")
    Map<Long, String> getTitles(@RequestBody List<Long> ids);
}
