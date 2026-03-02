package com.xuan.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 安全工具类
 * 用于获取当前登录用户信息
 *
 * @author 玄〤
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        HttpServletRequest request = getRequest();
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : null;
    }

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();
    }
}
