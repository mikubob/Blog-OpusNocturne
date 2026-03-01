package com.xuan.service.service;

import java.util.Map;

/**
 * 站点统计服务接口
 */
public interface IStatisticsService {
    /** 获取站点概览统计 */
    Map<String, Object> getOverview();

    /** 获取文章发布趋势 */
    Map<String, Object> getArticleTrend();

    /** 获取访问统计 */
    Map<String, Object> getVisitStats();
}
