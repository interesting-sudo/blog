package com.example.dzl.service.impl;

import com.example.dzl.enums.DocumentErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.service.DocumentService;
import com.example.dzl.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文档服务实现类
 */
@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    // 支持的图片格式
    private static final String[] SUPPORTED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final String[] SUPPORTED_IMAGE_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @Autowired
    private OSSService ossService;

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(DocumentErrorEnum.FILE_EMPTY);
        }

        // 2. 验证文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException(DocumentErrorEnum.FILE_NAME_EMPTY);
        }

        // 3. 获取文件扩展名并验证是否为图片格式
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex).toLowerCase();
        }

        // 验证文件扩展名
        boolean isSupportedExtension = false;
        for (String supportedExt : SUPPORTED_IMAGE_EXTENSIONS) {
            if (supportedExt.equals(extension)) {
                isSupportedExtension = true;
                break;
            }
        }
        if (!isSupportedExtension) {
            throw new BusinessException(DocumentErrorEnum.FILE_TYPE_NOT_SUPPORTED);
        }

        // 验证 ContentType（如果存在）
        String contentType = file.getContentType();
        if (contentType != null && !contentType.trim().isEmpty()) {
            boolean isSupportedContentType = false;
            String contentTypeLower = contentType.toLowerCase();
            for (String supportedType : SUPPORTED_IMAGE_CONTENT_TYPES) {
                if (supportedType.equals(contentTypeLower)) {
                    isSupportedContentType = true;
                    break;
                }
            }
            if (!isSupportedContentType) {
                // 不支持的 Content-Type
                throw new BusinessException(DocumentErrorEnum.FILE_CONTENT_TYPE_NOT_SUPPORTED);
            }
        }

        try {
            // 4. 生成唯一文件名（使用UUID避免文件名冲突）
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 5. 构建文件路径：images/年/月/文件名
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String filePath = String.format("images/%s/%s", datePath, uniqueFileName);

            // 6. 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 7. 如果没有 ContentType，根据扩展名设置
            String finalContentType = StringUtils.hasText(contentType)
                    ? contentType
                    : getImageContentTypeByExtension(extension);

            // 8. 上传到OSS
            String fileUrl = ossService.uploadFile(inputStream, filePath, finalContentType);

            log.info("图片上传成功: 原始文件名={}, 存储路径={}, URL={}",
                    originalFilename, filePath, fileUrl);

            return fileUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传图片失败: {}", e.getMessage(), e);
            throw new BusinessException(DocumentErrorEnum.FILE_UPLOAD_FAILED, "上传图片失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException(DocumentErrorEnum.FILE_URL_EMPTY);
        }

        try {
            boolean success = ossService.deleteFile(fileUrl);
            if (!success) {
                throw new BusinessException(DocumentErrorEnum.FILE_NOT_EXIST);
            }
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            throw new BusinessException(DocumentErrorEnum.FILE_DELETE_FAILED, "删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件扩展名获取图片的ContentType
     */
    private String getImageContentTypeByExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return "image/jpeg"; // 默认返回 jpeg
        }

        String ext = extension.toLowerCase();
        switch (ext) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            default:
                return "image/jpeg"; // 默认返回 jpeg
        }
    }
}
