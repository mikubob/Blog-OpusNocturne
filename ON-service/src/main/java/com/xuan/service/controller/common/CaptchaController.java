package com.xuan.service.controller.common;

import com.xuan.common.enums.CaptchaType;
import com.xuan.entity.dto.common.CaptchaResponse;
import com.xuan.service.service.ICaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 *
 * @author 玄〤
 * @since 2026-03-03
 */
@Tag(name = "验证码相关接口")
@RestController
@RequestMapping("/api/common/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final ICaptchaService captchaService;

    /**
     * 获取数学计算验证码
     *
     * @param type   验证码类型：LOGIN或COMMENT
     * @param uuid   登录场景下的临时标识，由前端生成
     * @param request 请求对象，用于获取IP地址
     * @return 验证码响应
     */
    @Operation(summary = "获取数学计算验证码")
    @GetMapping("/math")
    public CaptchaResponse getMathCaptcha(
            @Parameter(description = "验证码类型：LOGIN或COMMENT", required = true) @RequestParam("type") CaptchaType type,
            @Parameter(description = "登录场景下的临时标识，由前端生成") @RequestParam(value = "uuid", required = false) String uuid,
            HttpServletRequest request) {

        // 获取IP地址
        String ip = request.getRemoteAddr();

        // 对于LOGIN类型，必须提供uuid
        if (CaptchaType.LOGIN == type && (uuid == null || uuid.isEmpty())) {
            throw new IllegalArgumentException("登录场景下必须提供uuid");
        }

        // 对于COMMENT类型，从JWT中获取userId
        // 这里暂时使用临时方案，实际应该从JWT解析
        String identifier;
        if (CaptchaType.COMMENT == type) {
            // 实际项目中，这里应该从JWT Token中解析userId
            // 例如：String userId = JwtUtils.getUserIdFromToken(request);
            // 这里为了演示，暂时使用固定值
            identifier = "1";
        } else {
            identifier = uuid;
        }

        return captchaService.generateCaptcha(type, identifier, ip);
    }

}
