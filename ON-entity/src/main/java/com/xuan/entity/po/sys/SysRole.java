package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统角色表实体类
 * 对应数据库表：sys_role
 * 用于存储系统角色信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "系统角色表实体类")
public class SysRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色名称，唯一
     */
    @Schema(description = "角色名称，唯一", example = "超级管理员")
    private String roleName;

    /**
     * 角色编码，唯一
     */
    @Schema(description = "角色编码，唯一", example = "admin")
    private String roleCode;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "系统最高权限")
    private String description;

    /**
     * 状态：1-启用；0-禁用
     */
    @Schema(description = "状态：1-启用；0-禁用", example = "1")
    private Integer status;
}
