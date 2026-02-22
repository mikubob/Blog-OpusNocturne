/*
package com.xuan.service.aop;

import com.alibaba.fastjson2.JSON;
import com.xuan.entity.po.sys.SysOperLog;
import com.xuan.service.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

*/
/**
 * 接口日志切面
 * 自动记录所有 Controller 请求的方法名、参数、耗时以及持久化到操作日志表
 *
 * @author 玄〤
 * @since 2026-02-21
 *//*

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final SysOperLogMapper operLogMapper;

    */
/**
     * 切入点：所有 Controller 包下的方法
     *//*

    @Pointcut("execution(* com.xuan.service.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : "";
        String uri = request != null ? request.getRequestURI() : "";
        String ip = request != null ? request.getRemoteAddr() : "";
        String username = request != null ? (String) request.getAttribute("username") : "anonymous";

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("====> 请求: {} {} | IP: {} | 方法: {}.{}", method, uri, ip, className, methodName);

        Object result = null;
        Integer status = 1;
        String errorMsg = "";

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("{}: {}.{} | 耗时: {}ms", status == 1 ? "<==== 响应" : "<==== 异常", className, methodName, costTime);

            // 异步持久化操作日志 (目前先采用同步)
            // 记录所有管理端操作以及非 GET 请求
            if (uri.contains("/admin/") || !"GET".equalsIgnoreCase(method)) {
                saveLog(joinPoint, method, uri, ip, username, result, status, errorMsg, costTime);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, String method, String uri, String ip,
            String username, Object result, Integer status, String errorMsg, long costTime) {
        try {
            SysOperLog operLog = SysOperLog.builder()
                    .title(getControllerName(joinPoint))
                    .businessType(getBusinessType(method))
                    .method(joinPoint.getSignature().toShortString())
                    .requestMethod(method)
                    .operName(username)
                    .operUrl(uri)
                    .operIp(ip)
                    .operParam(JSON.toJSONString(joinPoint.getArgs()))
                    .jsonResult(result != null ? JSON.toJSONString(result) : null)
                    .status(status)
                    .errorMsg(errorMsg)
                    .operTime(LocalDateTime.now())
                    .costTime(costTime)
                    .build();
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getControllerName(ProceedingJoinPoint joinPoint) {
        io.swagger.v3.oas.annotations.tags.Tag tag = joinPoint.getTarget().getClass()
                .getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
        return tag != null ? tag.name() : joinPoint.getTarget().getClass().getSimpleName();
    }

    private String getBusinessType(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> "1";
            case "PUT" -> "2";
            case "DELETE" -> "3";
            default -> "0";
        };
    }
}
*/
