package com.xuan.entity.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审核评论请求参数类
 * 对应接口：7.3.2 审核评论
 * 用于接收前端审核评论的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "审核评论请求参数类")
public class CommentAuditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核状态：1-审核通过，2-审核未通过，必填
     */
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过，2-审核未通过", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer status;
}
