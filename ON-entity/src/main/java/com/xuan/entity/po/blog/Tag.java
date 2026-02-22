package com.xuan.entity.po.blog;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文章标签表实体类
 * 对应数据库表：tag
 * 用于存储文章标签信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
@Schema(description = "文章标签表实体类")
public class Tag extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称，唯一
     */
    @Schema(description = "标签名称，唯一", example = "Spring Boot")
    private String name;

    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;
}
