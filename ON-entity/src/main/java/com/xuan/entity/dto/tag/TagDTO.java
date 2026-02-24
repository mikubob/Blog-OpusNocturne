package com.xuan.entity.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签请求参数类
 * 对应接口：6.3.3创建标签，6.3.4更新标签
 * 用于接收前端创建或更新标签的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "标签请求参数类")
public class TagDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称，必填
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50字符")
    @Schema(description = "标签名称", example = "Spring Boot", requiredMode = RequiredMode.REQUIRED)
    private String name;

    /**
     * 标签颜色
     */
    @Size(max = 20, message = "标签颜色长度不能超过20字符")
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;
}
