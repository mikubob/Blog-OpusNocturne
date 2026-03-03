package com.xuan.entity.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码响应DTO
 * 用于返回验证码问题和key
 *
 * @author 玄〤
 * @since 2026-03-03
 */
@Schema(description = "验证码响应DTO")
public record CaptchaResponse(
        @Schema(description = "验证码问题", example = "8 × 5 = ?")
        String question,
        
        @Schema(description = "验证码key，登录场景需要返回供提交时使用", example = "captcha:login:127.0.0.1:uuid123")
        String captchaKey
) {
}
