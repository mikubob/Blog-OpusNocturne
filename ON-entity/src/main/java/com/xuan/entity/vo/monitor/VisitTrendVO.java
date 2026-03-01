package com.xuan.entity.vo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 访问趋势数据 VO
 *
 * @author 玄〤
 * @since 2026-02-28
 */
@Data
@Schema(description = "访问趋势数据")
public class VisitTrendVO {

    /**
     * 访问日期
     */
    @Schema(description = "访问日期")
    private LocalDate visitDate;

    /**
     * 页面浏览量
     */
    @Schema(description = "页面浏览量")
    private Long pv;

    /**
     * 独立访客数
     */
    @Schema(description = "独立访客数")
    private Long uv;
}