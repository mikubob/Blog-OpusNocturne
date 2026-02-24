package com.xuan.entity.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签后台管理响应数据类
 * 对应接口：6.3.2 后台标签管理
 * 用于返回标签的详细信息
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "标签后台管理响应数据类")
public class TagAdminVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 标签ID
     */
    @Schema(description = "标签ID", example = "1")
    private Long id;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "Spring Boot")
    private String name;

    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-17 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-02-17 10:00:00")
    private LocalDateTime updateTime;
}
