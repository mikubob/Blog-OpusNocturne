package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.entity.dto.auth.ChangePasswordDTO;
import com.xuan.entity.dto.auth.LoginDTO;
import com.xuan.entity.vo.auth.LoginVO;
import com.xuan.entity.vo.auth.UserInfoVO;
import com.xuan.service.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、退出登录、获取用户信息、刷新Token等
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        authService.logout(username);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return Result.success(authService.getUserInfo(username));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return Result.success(authService.refreshToken(username));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.changePassword(userId, dto);
        return Result.success(null, "密码修改成功，请重新登录");
    }
}
