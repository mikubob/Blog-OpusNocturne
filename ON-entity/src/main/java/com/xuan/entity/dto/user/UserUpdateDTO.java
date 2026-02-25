package com.xuan.entity.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 更新用户请求参数类
 * 对应接口：4.3 更新用户
 * 用于接收前端更新用户的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "更新用户请求参数类")
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "管理员")
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
     * 状态：1-启用，0-禁用
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
}
