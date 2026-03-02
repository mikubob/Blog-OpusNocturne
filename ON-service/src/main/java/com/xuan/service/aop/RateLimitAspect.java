package com.xuan.service.aop;

import com.xuan.service.annotation.RateLimit;
import com.xuan.common.enums.ErrorCode;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * <p>
 * 配合 {@link RateLimit} 注解使用，基于 Redis 实现滑动窗口计数限流。
 * 当请求超过限流阈值时，抛出 {@link BusinessException} 异常，
 * 由 GlobalExceptionHandler 统一返回友好提示。
 * <p>
 * 限流维度：IP + 接口方法（确保不同接口的限流互不影响）
 * <p>
 * Redis Key 格式：rate_limit:{IP}:{类名.方法名}
 *
 * @author 玄〤
 * @since 2026-03-02
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    /** Redis Key 前缀 */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    /**
     * 在标注了 @RateLimit 的方法执行前进行限流检查
     *
     * @param joinPoint 切入点
     * @param rateLimit 限流注解
     */
    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取客户端 IP
        String ip = IpUtils.getIpAddr(request);

        // 构建限流 Key：rate_limit:{IP}:{类名.方法名}
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodKey = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        String redisKey = RATE_LIMIT_KEY_PREFIX + ip + ":" + methodKey;

        // 获取限流参数
        int maxCount = rateLimit.maxCount();
        long period = rateLimit.period();
        TimeUnit timeUnit = rateLimit.timeUnit();

        // 使用 Redis INCR + EXPIRE 实现计数限流
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);

        if (currentCount != null) {
            if (currentCount == 1) {
                // 第一次请求，设置过期时间
                stringRedisTemplate.expire(redisKey, period, timeUnit);
            }

            if (currentCount > maxCount) {
                log.warn("接口限流触发 - IP: {}, 接口: {}, 当前次数: {}, 限制: {}/{}{}",
                        ip, methodKey, currentCount, maxCount, period, timeUnit.name().toLowerCase());

                // 获取限流提示信息
                String message = rateLimit.message().isEmpty()
                        ? ErrorCode.TOO_MANY_REQUESTS.getMessage()
                        : rateLimit.message();

                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS.getCode(), message);
            }
        }
    }
}
