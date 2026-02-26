package com.xuan.entity.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求参数类
 * 对应接口：3.1 用户登录
 * 用于接收前端登录请求的参数
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "登录请求参数类")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名，必填
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度应在2-50个字符之间")
    @Schema(description = "用户名，必填", example = "admin", requiredMode = RequiredMode.REQUIRED)
    private String username;
    
    /**
     * 密码，必填
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20个字符之间")
    @Schema(description = "密码，必填", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String password;
}