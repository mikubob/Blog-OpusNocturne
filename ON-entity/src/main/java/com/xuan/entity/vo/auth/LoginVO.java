package com.xuan.entity.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应数据类
 * 对应接口：3.1 用户登录响应
 * 用于返回登录成功后的令牌信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Builder
@Schema(description = "登录响应数据类")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * JWT令牌
     */
    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    /**
     * 令牌前缀
     */
    @Schema(description = "令牌前缀", example = "Bearer")
    private String tokenHead;
}