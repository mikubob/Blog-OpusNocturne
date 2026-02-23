package com.xuan.entity.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建分类请求参数类
 * 对应接口：6.2.2 创建分类
 * 用于接收前端创建分类的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "创建分类请求参数类")
public class CategoryCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称，必填
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50字符")
    @Schema(description = "分类名称", example = "Java", requiredMode = RequiredMode.REQUIRED)
    private String name;

    /**
     * 描述
     */
    @Size(max = 255, message = "分类描述长度不能超过255字符")
    @Schema(description = "分类描述", example = "Java相关技术")
    private String description;

    /**
     * 排序（升序）
     */
    @Schema(description = "排序（升序）", example = "0")
    private Integer sort;

    /**
     * 状态：1-启用，0-禁用 (默认1)
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
}
