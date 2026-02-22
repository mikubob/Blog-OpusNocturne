package com.xuan.entity.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台文章列表响应数据类
 * 对应接口5.2
 */
@Data
@Schema(description = "后台文章列表响应数据类")
public class ArticleAdminListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "封面图片")
    private String coverImg;

    @Schema(description = "浏览次数")
    private Long viewCount;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
