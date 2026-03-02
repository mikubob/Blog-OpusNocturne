package com.xuan.service.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * <p>
 * 在 Controller 方法上标注此注解，可实现基于 Redis 的接口级别限流。
 * 支持按 IP 或按用户维度限流，防止恶意请求和 DDoS 攻击。
 * <p>
 * 使用示例：
 * 
 * <pre>
 * // 每个 IP 每分钟最多请求 10 次
 * &#64;RateLimit(maxCount = 10, period = 1, timeUnit = TimeUnit.MINUTES)
 *
 * // 每个 IP 每秒最多请求 5 次
 * &#64;RateLimit(maxCount = 5, period = 1, timeUnit = TimeUnit.SECONDS)
 *
 * // 评论接口限流：每个 IP 每分钟最多 3 次
 * &#64;RateLimit(maxCount = 3, period = 1, timeUnit = TimeUnit.MINUTES, message = "评论太频繁，请稍后再试")
 * </pre>
 *
 * @author 玄〤
 * @since 2026-03-02
 * @see com.xuan.service.aop.RateLimitAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流时间窗口内的最大请求次数
     * 默认 10 次
     */
    int maxCount() default 10;

    /**
     * 限流时间窗口大小
     * 默认 1（配合 timeUnit 使用）
     */
    long period() default 1;

    /**
     * 限流时间窗口单位
     * 默认分钟
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 触发限流时的提示信息
     * 为空时使用默认提示："请求过于频繁，请稍后再试"
     */
    String message() default "";
}
