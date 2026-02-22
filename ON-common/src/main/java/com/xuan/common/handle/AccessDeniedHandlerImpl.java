package com.xuan.common.handle;

import com.alibaba.fastjson2.JSON;
import com.xuan.common.domain.Result;
import com.xuan.common.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 权限不足自定义处理器 (403 错误)
 * 
 * 当用户已登录，但试图访问其角色/权限范围之外的受保护资源时，Security 会触发此处理器。
 * 与默认跳转到错误页面不同，我们在此将其包装为统一的 Result JSON 格式返回。
 *
 * @author 玄〤
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * 实现权限不足的响应逻辑
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 构建 403 错误响应
        Result<?> result = Result.error(ErrorCode.FORBIDDEN);
        String json = JSON.toJSONString(result);

        // 设置 HTTP 状态码为 200（由业务码区分错误），并指定 JSON 格式
        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        // 将 JSON 直接写入响应流
        response.getWriter().print(json);
    }
}
