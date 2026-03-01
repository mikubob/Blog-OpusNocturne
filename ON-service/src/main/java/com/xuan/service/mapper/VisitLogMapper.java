package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.sys.VisitLog;
import com.xuan.entity.vo.monitor.VisitTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访问记录 Mapper
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    /**
     * 统计指定时间范围内的独立访客数（UV）
     */
    Long countUniqueVisitors(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计指定时间范围内的页面浏览量（PV）
     */
    Long countPageViews(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 获取每日访问趋势数据
     */
    List<VisitTrendVO> getVisitTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
