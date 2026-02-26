package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.entity.dto.system.SystemSettingDTO;
import com.xuan.entity.vo.system.SystemSettingVO;
import com.xuan.service.service.ISysSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置控制器
 */
@Tag(name = "系统设置")
@RestController
@RequestMapping("/api/admin/setting")
@RequiredArgsConstructor
public class SettingController {

    private final ISysSettingService sysSettingService;

    @Operation(summary = "获取系统设置")
    @GetMapping
    public Result<SystemSettingVO> getSettings() {
        return Result.success(sysSettingService.getSettings());
    }

    @Operation(summary = "更新系统设置")
    @PutMapping
    public Result<Void> updateSettings(@Validated @RequestBody SystemSettingDTO dto) {
        sysSettingService.updateSettings(dto);
        return Result.success();
    }
}