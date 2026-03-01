package com.xuan.entity.vo.monitor;

import lombok.Data;

import java.time.LocalDate;

/**
 * 访问趋势数据 VO
 *
 * @author 玄〤
 * @since 2026-02-28
 */
@Data
public class VisitTrendVO {

    /**
     * 访问日期
     */
    private LocalDate visitDate;

    /**
     * 页面浏览量
     */
    private Long pv;

    /**
     * 独立访客数
     */
    private Long uv;
}