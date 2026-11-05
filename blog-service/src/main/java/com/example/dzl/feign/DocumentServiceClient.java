package com.example.dzl.feign;

import com.example.dzl.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档服务 Feign 客户端
 */
@FeignClient(name = "document-service")
public interface DocumentServiceClient {

    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping(value = "/document/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadImage(@RequestPart("file") MultipartFile file);
}

