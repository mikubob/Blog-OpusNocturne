package com.xuan.entity.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量审核评论请求参数类
 * 对应接口：7.3.4 批量审核评论
 * @author 玄〤
 * @since 2026-02-20
 */
@Data
@Schema(description = "批量审核评论请求参数类")
public class CommentBatchAuditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "请选择要审核的评论")
    @Schema(description = "评论ID列表", example = "[1, 2, 3]", requiredMode = RequiredMode.REQUIRED)
    private List<Long> ids;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过，2-审核未通过", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer status;
}
