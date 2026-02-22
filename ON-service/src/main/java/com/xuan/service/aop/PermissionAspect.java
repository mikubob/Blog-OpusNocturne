/*
package com.xuan.service.aop;

import com.xuan.common.enums.ErrorCode;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.service.mapper.SysPermissionMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

*/
/**
 * 权限校验切面
 * 配合自定义注解 @RequirePermission 实现接口级权限控制
 *
 * @author 玄〤
 * @since 2026-02-20
 *//*

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionAspect {

    private final SysPermissionMapper sysPermissionMapper;

    */
/**
     * 自定义权限注解
     * 标注在 Controller 方法上，指定需要的权限码
     *//*

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequirePermission {
        */
/** 所需权限码，如 "blog:article:add" *//*

        String value();
    }

    */
/**
     * 在带有 @RequirePermission 注解的方法执行前校验权限
     *//*

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String requiredPerm = requirePermission.value();

        // 从请求中获取当前用户ID（由 JwtInterceptor 设置）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        HttpServletRequest request = attributes.getRequest();
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = Long.valueOf(userIdObj.toString());

        // 查询用户权限列表
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(userId);

        // 校验权限
        if (!permissions.contains(requiredPerm)) {
            log.warn("用户 {} 无权限: {}", userId, requiredPerm);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
*/
