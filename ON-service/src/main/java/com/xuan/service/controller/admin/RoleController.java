package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.entity.po.sys.SysRole;
import com.xuan.service.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台角色管理控制器
 */
@Tag(name = "后台角色管理")
@RestController
@RequestMapping("/api/admin/role")
@RequiredArgsConstructor
public class RoleController {

    private final ISysRoleService roleService;

    @Operation(summary = "获取所有角色")
    @GetMapping("/list")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleService.list());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getRoleDetail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Void> createRole(@Validated @RequestBody SysRole role) {
        roleService.save(role);
        return Result.success();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Validated @RequestBody SysRole role) {
        role.setId(id);
        roleService.updateById(role);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "分配角色权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @Operation(summary = "获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }
}