package com.xuan.entity.dto.tag;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 标签分页查询请求参数类
 * 对应接口：6.3.2 分页获取标签列表
 * 用于接收前端分页查询标签的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签分页查询请求参数类")
public class TagPageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称搜索
     */
    @Size(max = 50, message = "标签名称搜索长度不能超过50个字符")
    @Schema(description = "标签名称搜索", example = "Spring")
    private String name;
}
