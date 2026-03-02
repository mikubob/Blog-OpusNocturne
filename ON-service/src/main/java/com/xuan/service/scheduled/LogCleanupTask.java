package com.xuan.service.scheduled;

import com.xuan.service.mapper.SysOperLogMapper;
import com.xuan.service.mapper.VisitLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 日志清理定时任务
 * 每三天清理一次过期的操作日志和访问日志
 *
 * @author 玄〤
 * @since 2026-03-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogCleanupTask {

    private final SysOperLogMapper sysOperLogMapper;
    private final VisitLogMapper visitLogMapper;

    /**
     * 每三天凌晨0点执行日志清理
     * 表达式：0 0 0 * / 3 * * ? (实际使用时去掉空格)
     */
    @Scheduled(cron = "0 0 0 */3 * ?")
    public void cleanupExpiredLogs() {
        log.info("开始执行日志清理任务...");

        try {
            // 计算3天前的时间
            LocalDateTime threeDaysAgo = LocalDateTime.now().minus(3, ChronoUnit.DAYS);

            // 清理操作日志
            int operLogCount = sysOperLogMapper.deleteByOperTimeBefore(threeDaysAgo);
            log.info("清理操作日志 {} 条", operLogCount);

            // 清理访问日志
            int visitLogCount = visitLogMapper.deleteByVisitTimeBefore(threeDaysAgo);
            log.info("清理访问日志 {} 条", visitLogCount);

            log.info("日志清理任务执行完成，共清理 {} 条日志", operLogCount + visitLogCount);
        } catch (Exception e) {
            log.error("日志清理任务执行失败：{}", e.getMessage(), e);
        }
    }
}
