package com.xuan.common.handle;

import com.alibaba.fastjson2.JSON;
import com.xuan.common.domain.Result;
import com.xuan.common.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证失败自定义处理器 (401 错误)
 * <p>
 * 当匿名用户尝试访问受保护资源，或者携带的 Token 已失效/错误时，Security 会触发此处理器。
 * 我们统一返回 401 错误码，引导前端清除缓存并跳转到登录页。
 *
 * @author 玄〤
 * @since 2026-02-18
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * 实现认证失败的响应逻辑
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        // 构建 401 错误响应
        Result<?> result = Result.error(ErrorCode.UNAUTHORIZED);
        String json = JSON.toJSONString(result);

        // 设置 HTTP 状态码为 200（由业务码区分错误），并指定 JSON 格式
        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        // 将 JSON 直接写入响应流
        response.getWriter().print(json);
    }
}
