package com.xuan.entity.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章置顶请求参数类
 * 对应接口：5.6 文章置顶/取消置顶
 * 用于接收前端设置文章是否置顶的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "文章置顶请求参数类")
public class ArticleTopDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否置顶：1-是，0-否
     */
    @NotNull(message = "是否置顶不能为空")
    @Schema(description = "是否置顶：1-是，0-否", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer isTop;
}
