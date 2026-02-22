package com.xuan.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.nimbusds.jwt.JWTClaimsSet;
import com.xuan.common.constant.RedisConstant;
import com.xuan.common.domain.Result;
import com.xuan.common.enums.ErrorCode;
import com.xuan.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * <p>
 * 验证请求中的 JWT Token 合法性，并将用户信息存储到请求属性中。
 * 所有错误均返回统一的 Result JSON 格式，HTTP 状态码统一 200，由业务码区分。
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // OPTIONS 请求直接放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取令牌
        String token = request.getHeader(jwtUtils.getHeader());

        // 检查令牌是否存在
        if (token == null || token.isEmpty()) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        // 移除 Bearer 前缀
        String tokenPrefix = jwtUtils.getTokenPrefix();
        if (token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }

        try {
            // 解析并验证令牌
            JWTClaimsSet claimsSet = jwtUtils.getAllClaimsFromToken(token);
            String username = claimsSet.getSubject();

            // 检查 Redis 中是否存在该 Token（登出后 Token 会从 Redis 中删除）
            String redisKey = RedisConstant.TOKEN_KEY_PREFIX + username;
            String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedToken == null) {
                // Token 不在 Redis 中，可能已登出或已在其他设备登录
                writeErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
                return false;
            }

            // 如果 Redis 中存的 Token 和当前请求的 Token 不同，说明用户在其他设备重新登录了
            if (!storedToken.equals(token)) {
                writeErrorResponse(response, ErrorCode.TOKEN_REPLACED);
                return false;
            }

            // 将用户信息存储到请求属性中，方便后续使用
            request.setAttribute("userClaims", claimsSet);
            request.setAttribute("username", username);
            request.setAttribute("userId", claimsSet.getClaim("userId"));

            return true;
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.TOKEN_INVALID);
            return false;
        }
    }

    /**
     * 向前端返回统一格式的错误响应
     * <p>
     * HTTP 状态码统一返回 200，由 Result 中的业务 code 区分错误类型，
     * 前端根据 code 进行相应处理（如 2001 跳转登录页，2003 提示无权限等）
     */
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<?> result = Result.error(errorCode);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
