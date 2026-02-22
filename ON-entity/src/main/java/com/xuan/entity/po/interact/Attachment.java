package com.xuan.entity.po.interact;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 附件/资源表实体类
 * 对应数据库表：attachment
 * 用于存储上传的文件信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attachment")
@Schema(description = "附件/资源表实体类")
public class Attachment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原文件名
     */
    @Schema(description = "原文件名", example = "cover.png")
    private String fileName;

    /**
     * 访问URL
     */
    @Schema(description = "访问URL", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/2026-02-16/cover.png")
    private String fileUrl;

    /**
     * 储存路径
     */
    @Schema(description = "储存路径", example = "/uploads/2026/02/16/cover.png")
    private String filePath;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型", example = "image/png")
    private String fileType;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)", example = "102400")
    private Long fileSize;

    /**
     * 业务类型(article/avatar)
     */
    @Schema(description = "业务类型(article/avatar)", example = "article")
    private String bizType;

    /**
     * 业务id
     */
    @Schema(description = "业务id", example = "100")
    private Long bizId;
}
