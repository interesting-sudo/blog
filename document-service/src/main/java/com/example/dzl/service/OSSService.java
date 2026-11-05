package com.example.dzl.service;

import java.io.InputStream;

/**
 * 阿里云 OSS 服务接口
 */
public interface OSSService {

    /**
     * 上传文件到 OSS
     * @param inputStream 文件输入流
     * @param fileName 文件名（包含路径，如：images/2026/01/xxx.jpg）
     * @param contentType 文件类型（MIME类型，如：image/jpeg）
     * @return 文件的访问URL
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType);

    /**
     * 删除 OSS 中的文件
     * @param fileUrl 文件的完整URL或文件路径（相对于bucket的路径）
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 从URL中提取文件路径（相对于bucket的路径）
     * @param fileUrl 文件的完整URL
     * @return 文件路径（相对于bucket）
     */
    String extractFilePathFromUrl(String fileUrl);
}
