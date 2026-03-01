package com.xuan.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.entity.po.sys.VisitLog;
import com.xuan.entity.vo.monitor.VisitTrendVO;
import com.xuan.service.mapper.VisitLogMapper;
import com.xuan.service.service.IVisitLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xuan.common.constant.RedisConstant.STATS_PV_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.STATS_TOTAL_PV_KEY;
import static com.xuan.common.constant.RedisConstant.STATS_TTL_DAYS;
import static com.xuan.common.constant.RedisConstant.STATS_UV_KEY_PREFIX;

/**
 * 站点统计服务实现类
 * 数据来源：
 * - 文章/分类/标签/评论/用户数量：数据库查询
 * - 浏览量/UV/PV：Redis 实时统计（通过 IVisitLogService）
 * - 访问趋势：数据库 visit_log 表聚合
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitLogServiceImpl extends ServiceImpl<VisitLogMapper, VisitLog> implements IVisitLogService {

    private final StringRedisTemplate redisTemplate;
    private final VisitLogMapper visitLogMapper;

    /**
     * 记录访问
     * @param ipAddress IP地址
     * @param userAgent 设备信息
     * @param pageUrl 访问页面URL
     * @param referer 来源URL
     */
    @Async
    @Override
    public void recordVisit(String ipAddress, String userAgent, String pageUrl, String referer) {

        try {
            //1.写入数据库（持久化日志记录）
            VisitLog visitLog = new VisitLog();
            visitLog.setIpAddress(ipAddress);
            visitLog.setUserAgent(userAgent);
            visitLog.setPageUrl(pageUrl);
            visitLog.setReferer(referer);
            visitLog.setVisitTime(LocalDateTime.now());
            save(visitLog);

            //2.Redis计数-今日PV
            String today = LocalDate.now().toString();
            String pvKey=STATS_PV_KEY_PREFIX+today;
            redisTemplate.opsForValue().increment(pvKey);//递增1
            redisTemplate.expire(pvKey, STATS_TTL_DAYS, TimeUnit.DAYS);//缓存有效期

            //3.Redis计数 HyperLogLog(HyperLogLog:该作用是统计一组数据中不同元素的数量)-今日UV(基于IP地址去重)
            String uvKey=STATS_UV_KEY_PREFIX+today;
            redisTemplate.opsForHyperLogLog().add(uvKey, ipAddress);
            redisTemplate.expire(uvKey, STATS_TTL_DAYS, TimeUnit.DAYS);

            //4.全站总PV
            redisTemplate.opsForValue().increment(STATS_TOTAL_PV_KEY);
        } catch (Exception e) {
            log.error("记录访问日志失败：{}", e.getMessage(),e);
        }
    }

    /**
     * 获取今日PV(pv: page view, 访问页面量)
     * @return 今日PV
     */
    @Override
    public Long getTodayPV() {
        String pvKey=STATS_PV_KEY_PREFIX+LocalDate.now().toString();
        String val=redisTemplate.opsForValue().get(pvKey);
        return val==null?0L:Long.parseLong(val);
    }

    /**
     * 获取今日UV(uv: unique visitor, 访问用户量)
     * @return 今日UV
     */
    @Override
    public Long getTodayUV() {
        String uvKey=STATS_UV_KEY_PREFIX+LocalDate.now().toString();
        Long size = redisTemplate.opsForHyperLogLog().size(uvKey);
        return size!=null?size:0L;
    }

    /**
     * 获取总PV(pv: page view, 访问页面量)
     * @return 总PV
     */
    @Override
    public Long getTotalPV() {
        String val=redisTemplate.opsForValue().get(STATS_TOTAL_PV_KEY);
        return val==null?0L:Long.parseLong(val);
    }

    /**
     * 获取总UV(uv: unique visitor, 访问用户量)
     * @param days 天数
     * @return 总UV
     */
    @Override
    public List<VisitTrendVO> getVisitTrend(int days) {
        LocalDateTime end=LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime start=LocalDate.now().minusDays(days-1).atStartOfDay();
        return visitLogMapper.getVisitTrend(start,end);
    }

    /**
     * 获取热门页面
     * @param days 天数
     * @param limit 限制数量
     * @return 热门页面列表
     */
    @Override
    public List<Map<String, Object>> getTopPages(int days, int limit) {
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime start = LocalDate.now().minusDays(days - 1).atStartOfDay();
        return visitLogMapper.getTopPages(start, end, limit);
    }
}
