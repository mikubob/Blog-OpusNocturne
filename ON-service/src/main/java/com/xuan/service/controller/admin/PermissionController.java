package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.entity.po.sys.SysPermission;
import com.xuan.service.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台权限管理控制器
 */
@Tag(name = "后台权限管理")
@RestController
@RequestMapping("/api/admin/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final ISysPermissionService permissionService;

    @Operation(summary = "获取所有权限（树形结构）")
    @GetMapping("/tree")
    public Result<List<SysPermission>> getPermissionTree() {
        return Result.success(permissionService.list());
    }

    @Operation(summary = "创建权限")
    @PostMapping
    public Result<Void> createPermission(@Validated @RequestBody SysPermission permission) {
        permissionService.save(permission);
        return Result.success();
    }

    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<Void> updatePermission(@PathVariable Long id, @Validated @RequestBody SysPermission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return Result.success();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success();
    }
}