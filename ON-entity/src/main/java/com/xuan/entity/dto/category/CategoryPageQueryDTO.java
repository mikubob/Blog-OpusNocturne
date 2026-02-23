package com.xuan.entity.dto.category;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 分类分页查询请求参数类
 * 对应接口：6.2.1 分页获取分类列表
 * 用于接收前端分页查询分类的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分类分页查询请求参数类")
public class CategoryPageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称搜索
     */
    @Size(max = 50, message = "分类名称搜索长度不能超过50个字符")
    @Schema(description = "分类名称搜索", example = "Java")
    private String name;
}
