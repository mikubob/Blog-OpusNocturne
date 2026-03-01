package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.service.service.IStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 站点统计控制器
 */
@Tag(name = "站点统计")
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @Operation(summary = "获取站点概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @Operation(summary = "获取文章发布趋势")
    @GetMapping("/article-trend")
    public Result<Map<String, Object>> getArticleTrend() {
        return Result.success(statisticsService.getArticleTrend());
    }

    @Operation(summary = "获取访问统计")
    @GetMapping("/visit")
    public Result<Map<String, Object>> getVisitStats(@RequestParam(required = false,defaultValue = "10") Integer topPagesLimit) {
        return Result.success(statisticsService.getVisitStats(topPagesLimit));
    }
}