package com.xuan.service.service;

import com.xuan.common.enums.CaptchaType;
import com.xuan.entity.dto.common.CaptchaResponse;

/**
 * 验证码服务接口
 *
 * @author 玄〤
 * @since 2026-03-03
 */
public interface ICaptchaService {

    /**
     * 生成验证码
     *
     * @param type       验证码类型
     * @param identifier 标识符
     *                   - LOGIN类型：前端生成的uuid
     *                   - COMMENT类型：用户id
     * @param ip         IP地址（用于LOGIN类型构建key）
     * @return 验证码响应
     */
    CaptchaResponse generateCaptcha(CaptchaType type, String identifier, String ip);

    /**
     * 验证验证码
     *
     * @param type       验证码类型
     * @param identifier 标识符
     *                   - LOGIN类型：完整的captchaKey
     *                   - COMMENT类型：用户id
     * @param answer     验证码答案
     * @return 是否验证通过
     */
    boolean validateCaptcha(CaptchaType type, String identifier, String answer);

}
