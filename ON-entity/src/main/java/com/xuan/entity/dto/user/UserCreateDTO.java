package com.xuan.entity.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建用户请求参数类
 * 对应接口：4.2 创建用户
 * 用于接收前端创建用户的参数
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "创建用户请求参数类")
public class UserCreateDTO implements Serializable {

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
     * 初始密码，必填
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20个字符之间")
    @Schema(description = "初始密码，必填", example = "Password123", requiredMode = RequiredMode.REQUIRED)
    private String password;
    
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "玄〤")
    private String nickname;
    
    /**
     * 邮箱
     */
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 关联角色ID列表
     */
    @Schema(description = "关联角色ID列表", example = "[1, 2]")
    private List<Long> roleIds;
    
    /**
     * 状态：1-启用，0-禁用 (默认1)
     */
    @Schema(description = "状态：1-启用，0-禁用 (默认1)", example = "1")
    private Integer status;
}