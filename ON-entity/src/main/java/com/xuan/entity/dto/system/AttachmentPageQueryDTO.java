package com.xuan.entity.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 附件分页查询参数
 * 对应接口：13.2 分页获取附件列表 (Admin)
 * @author 玄〤
 * @since 2026-02-27
 */
@Data
@Schema(description = "附件分页查询参数")
public class AttachmentPageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;

    @Schema(description = "文件名称（模糊查询）")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;
}
