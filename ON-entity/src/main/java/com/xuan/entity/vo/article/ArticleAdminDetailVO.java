package com.xuan.entity.vo.article;

import com.xuan.entity.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台文章详情响应数据类
 * 对应接口文档5.5
 * 用于编辑时获取文章完整信息
 */
@Data
@Schema(description = "后台文章详情响应数据类")
public class ArticleAdminDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "封面图片")
    private String coverImg;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
