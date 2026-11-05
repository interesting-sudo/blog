package com.example.dzl.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.example.dzl.config.OSSConfig;
import com.example.dzl.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * 阿里云 OSS 服务实现类
 */
@Service
@Slf4j
public class OSSServiceImpl implements OSSService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OSSConfig ossConfig;

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        if (inputStream == null || !StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件流和文件名不能为空");
        }

        try {
            // 确保文件路径不以 / 开头
            String objectName = fileName.startsWith("/") ? fileName.substring(1) : fileName;

            // 上传文件
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);

            // 构建文件访问URL
            String fileUrl;
            if (StringUtils.hasText(ossConfig.getUrlPrefix())) {
                // 如果配置了URL前缀，使用配置的前缀
                fileUrl = ossConfig.getUrlPrefix().endsWith("/")
                        ? ossConfig.getUrlPrefix() + objectName
                        : ossConfig.getUrlPrefix() + "/" + objectName;
            } else {
                // 否则使用OSS默认的URL格式
                fileUrl = String.format("https://%s.%s/%s",
                        ossConfig.getBucketName(),
                        ossConfig.getEndpoint().replace("https://", "").replace("http://", ""),
                        objectName);
            }

            log.info("文件上传成功: {}, URL: {}", objectName, fileUrl);
            return fileUrl;
        } catch (OSSException e) {
            log.error("OSS上传文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("上传文件时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        try {
            // 从URL中提取文件路径
            String objectName = extractFilePathFromUrl(fileUrl);

            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
            if (!exists) {
                log.warn("文件不存在，无需删除: {}", objectName);
                return false;
            }

            // 删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
            log.info("文件删除成功: {}", objectName);
            return true;
        } catch (OSSException e) {
            log.error("OSS删除文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("删除文件时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractFilePathFromUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return "";
        }

        // 如果配置了URL前缀，尝试从URL中提取路径
        if (StringUtils.hasText(ossConfig.getUrlPrefix())) {
            String prefix = ossConfig.getUrlPrefix();
            if (fileUrl.startsWith(prefix)) {
                String path = fileUrl.substring(prefix.length());
                return path.startsWith("/") ? path.substring(1) : path;
            }
        }

        // 尝试从标准OSS URL格式中提取
        // 格式: https://bucket-name.endpoint/path/to/file
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            try {
                // 移除协议前缀
                String urlWithoutProtocol = fileUrl.replaceFirst("^https?://", "");

                // 查找第一个 / 之后的部分
                int firstSlashIndex = urlWithoutProtocol.indexOf("/");
                if (firstSlashIndex > 0) {
                    // 检查是否是 bucket.endpoint 格式
                    String bucketAndEndpoint = urlWithoutProtocol.substring(0, firstSlashIndex);
                    if (bucketAndEndpoint.contains(".")) {
                        // 是 bucket.endpoint 格式，提取路径部分
                        String path = urlWithoutProtocol.substring(firstSlashIndex + 1);
                        return path;
                    }
                }
            } catch (Exception e) {
                log.warn("从URL提取文件路径失败，使用原始URL: {}", fileUrl);
            }
        }

        // 如果无法解析，假设传入的就是文件路径
        return fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
    }
}
