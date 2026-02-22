package com.xuan.entity.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章状态更新请求参数类
 * 对应接口：5.7 更新文章状态
 * 用于接收前端更新文章状态的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "文章状态更新请求参数类")
public class ArticleStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态：0-草稿，1-发布，2-下架
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-草稿，1-发布，2-下架", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer status;
}
