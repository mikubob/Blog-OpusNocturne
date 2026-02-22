package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-角色关联表实体类
 * 对应数据库表：sys_user_role
 * 用于存储用户与角色的关联关系
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户-角色关联表实体类")
public class SysUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Schema(description = "主键id", example = "1")
    private Long id;

    /**
     * 用户id
     */
    @Schema(description = "用户id", example = "1")
    private Long userId;

    /**
     * 角色id
     */
    @Schema(description = "角色id", example = "1")
    private Long roleId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-17T10:00:00")
    private LocalDateTime createTime;
}