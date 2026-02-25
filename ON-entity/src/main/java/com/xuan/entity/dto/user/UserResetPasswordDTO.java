package com.xuan.entity.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重置用户密码请求参数类
 * 对应接口：4.6 重置用户密码
 * 用于接收前端重置用户密码的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "重置用户密码请求参数类")
public class UserResetPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20个字符之间")
    @Schema(description = "新密码", example = "NewPassword123", requiredMode = RequiredMode.REQUIRED)
    private String password;
}
