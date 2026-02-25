package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.user.UserCreateDTO;
import com.xuan.entity.dto.user.UserPageQueryDTO;
import com.xuan.entity.dto.user.UserResetPasswordDTO;
import com.xuan.entity.dto.user.UserUpdateDTO;
import com.xuan.entity.vo.auth.UserInfoVO;
import com.xuan.entity.vo.user.UserListVO;
import com.xuan.service.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理控制器
 */
@Tag(name = "后台用户管理")
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final ISysUserService sysUserService;

    @Operation(summary = "分页获取用户列表")
    @GetMapping("/page")
    public Result<Page<UserListVO>> pageUsers(@Validated UserPageQueryDTO dto) {
        return Result.success(sysUserService.pageUsers(dto));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Void> createUser(@Validated @RequestBody UserCreateDTO dto) {
        sysUserService.createUser(dto);
        return Result.success();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Validated @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserInfoVO> getUserDetail(@PathVariable Long id) {
        return Result.success(sysUserService.getUserDetail(id));
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @Validated @RequestBody UserResetPasswordDTO dto) {
        sysUserService.resetPassword(id, dto);
        return Result.success();
    }
}