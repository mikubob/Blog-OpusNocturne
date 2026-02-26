package com.xuan.entity.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应数据类
 * 对应接口：4.3 获取当前用户信息
 * 用于返回当前登录用户的详细信息
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Builder
@Schema(description = "用户信息响应数据类")
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "玄〤")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/avatar.png")
    private String avatar;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 状态：1-启用，0-禁用
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;

    /**
     * 角色ID列表
     */
    @Schema(description = "角色ID列表", example = "[1, 2]")
    private List<Long> roleIds;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 权限列表
     */
    @Schema(description = "权限列表", example = "[\"sys:user:list\",\"sys:user:add\"]")
    private List<String> permissions;
}