package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.po.sys.VisitLog;
import com.xuan.entity.vo.monitor.VisitTrendVO;

import java.util.List;
import java.util.Map;

/**
 * 访问记录服务接口
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public interface IVisitLogService extends IService<VisitLog> {

    /**
     * 记录访问日志（异步），使用 Redis 缓冲减少数据库写入
     */
    void recordVisit(String ipAddress, String userAgent, String pageUrl, String referer);

    /**
     * 获取今日 PV
     */
    Long getTodayPV();

    /**
     * 获取今日 UV
     */
    Long getTodayUV();

    /**
     * 获取总 PV
     */
    Long getTotalPV();

    /**
     * 获取过去 N 天的访问趋势
     */
    List<VisitTrendVO> getVisitTrend(int days);

    /**
     * 获取热门页面
     * @param days 天数
     * @param limit 限制数量
     * @return 热门页面列表
     */
    List<Map<String, Object>> getTopPages(int days, int limit);
}