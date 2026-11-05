package com.example.dzl.contrroller;

import com.example.dzl.service.DocumentService;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档管理控制器
 */
@RestController
@RequestMapping("/document")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * 上传图片
     * @param file 上传的图片文件
     * @return 图片的访问URL
     */
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("收到图片上传请求: 文件名={}, 大小={}",
                file.getOriginalFilename(), file.getSize());

        String fileUrl = documentService.uploadImage(file);
        return Result.success("图片上传成功", fileUrl);
    }

    /**
     * 删除文件
     * @param fileUrl 文件的访问URL
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        log.info("收到文件删除请求: fileUrl={}", fileUrl);

        documentService.deleteFile(fileUrl);
        return Result.success();
    }
}
