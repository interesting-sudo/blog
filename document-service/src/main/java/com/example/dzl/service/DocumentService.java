package com.example.dzl.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文档服务接口
 */
public interface DocumentService {

    /**
     * 上传图片
     * @param file 上传的图片文件
     * @return 图片的访问URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 删除文件
     * @param fileUrl 文件的访问URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}
