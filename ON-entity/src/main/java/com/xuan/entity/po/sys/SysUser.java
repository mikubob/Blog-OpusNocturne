package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统用户表实体类
 * 对应数据库表：sys_user
 * 用于存储系统用户的基本信息
 * @author 玄〤
 * @since 2026-02-16
 */
@EqualsAndHashCode(callSuper = true)// 继承父类属性
@Data
@TableName("sys_user")
@Schema(description = "系统用户表实体类")
public class SysUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名，唯一
     */
    @Schema(description = "用户名，唯一", example = "admin")
    private String username;

    /**
     * 加密后的密码（BCrypt）
     */
    @Schema(description = "加密后的密码（BCrypt）", example = "$2a$10$...")
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "玄〤")
    private String nickname;

    /**
     * 邮箱，唯一
     */
    @Schema(description = "邮箱，唯一", example = "admin@example.com")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/avatar.png")
    private String avatar;

    /**
     * 状态：1-启用；0-禁用
     */
    @Schema(description = "状态：1-启用；0-禁用", example = "1")
    private Integer status;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2026-02-17 10:00:00")
    private LocalDateTime lastLoginTime;
}
