package com.example.dzl.feign;

import com.example.dzl.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ai-service")
public interface AiServiceClient {

    /**
     * 检查内容是否包含敏感词
     * @param body 包含 content 字段的请求体
     * @return true 表示包含敏感词，false 表示安全
     */
    @PostMapping("/ai/sensitive")
    Result<Boolean> hasSensitiveWord(@RequestBody Map<String, String> body);
}
