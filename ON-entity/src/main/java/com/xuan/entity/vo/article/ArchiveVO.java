package com.xuan.entity.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章归档 VO
 * 对应接口14.1
 * 
 * @author 玄〤
 */
@Data
@Schema(description = "文章归档响应数据")
public class ArchiveVO implements Serializable {

    @Schema(description = "年份", example = "2023")
    private String year;

    @Schema(description = "月份列表")
    private List<ArchiveMonthVO> months;

    @Data
    @Schema(description = "按月归档")
    public static class ArchiveMonthVO implements Serializable {
        @Schema(description = "月份", example = "10")
        private String month;

        @Schema(description = "文章数量", example = "5")
        private Integer count;

        @Schema(description = "文章列表")
        private List<ArchiveArticleVO> articles;
    }

    @Data
    @Schema(description = "归档文章简要信息")
    public static class ArchiveArticleVO implements Serializable {
        @Schema(description = "文章ID", example = "1")
        private Long id;

        @Schema(description = "文章标题", example = "文章标题")
        private String title;

        @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
        private String createTime;

        @Schema(description = "日", example = "01")
        private String day;
    }
}
