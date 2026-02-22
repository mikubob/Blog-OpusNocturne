package com.xuan.entity.dto.article;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 后台文章分页查询参数类
 * 对应接口5.2
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台文章分页查询参数类")
public class ArticleAdminPageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文章标题搜索", example = "Spring")
    private String title;

    @Schema(description = "按分类筛选", example = "1")
    private Long categoryId;

    @Schema(description = "按状态筛选", example = "1")
    private Integer status;
}
