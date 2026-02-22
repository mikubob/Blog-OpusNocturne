package com.xuan.common.utils;

import com.xuan.common.enums.ErrorCode;
import com.xuan.common.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传工具类
 * 处理文件的上传、类型校验、大小校验等
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Component
public class UploadUtils {

    /** 上传文件根目录 */
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /** 文件访问URL前缀 */
    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /** 允许上传的文件类型 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", // 图片
            "md", "txt", "pdf", // 文档
            "zip", "rar" // 压缩包
    );

    /** 最大文件大小: 10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 上传文件
     *
     * @param file 待上传的文件
     * @return 文件访问URL
     */
    public String upload(MultipartFile file) {
        // 校验文件
        validateFile(file);

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        // 生成存储路径: uploads/2026/02/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = datePath + "/" + newFileName;

        // 创建目录并保存
        try {
            Path targetPath = Paths.get(uploadPath, relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return urlPrefix + "/" + relativePath;
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        // 校验文件类型
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取文件MIME类型
     */
    public String getContentType(String filename) {
        String ext = getExtension(filename);
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "md", "txt" -> "text/plain";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            default -> "application/octet-stream";
        };
    }

    /**
     * 获取文件存储的物理路径
     */
    public String getStoragePath(String fileUrl) {
        if (fileUrl == null)
            return null;
        String relativePath = fileUrl.replace(urlPrefix, "");
        return Paths.get(uploadPath, relativePath).toString();
    }
}
