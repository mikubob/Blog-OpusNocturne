package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统权限/菜单表实体类
 * 对应数据库表：sys_permission
 * 用于存储系统菜单和权限信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
@Schema(description = "系统权限/菜单表实体类")
public class SysPermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1;

    /**
     * 父级id(0表示顶级)
     */
    @Schema(description = "父级id(0表示顶级)", example = "0")
    private Long parentId;

    /**
     * 菜单/权限名称
     */
    @Schema(description = "菜单/权限名称", example = "系统管理")
    private String name;

    /**
     * 权限标识，唯一
     */
    @Schema(description = "权限标识，唯一", example = "system")
    private String code;

    /**
     * 类型：1-菜单；2-按钮
     */
    @Schema(description = "类型：1-菜单；2-按钮", example = "1")
    private Integer type;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "system/index")
    private String component;

    /**
     * 图标
     */
    @Schema(description = "图标", example = "setting")
    private String icon;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 状态：1-启用；0-禁用
     */
    @Schema(description = "状态：1-启用；0-禁用", example = "1")
    private Integer status;
}
