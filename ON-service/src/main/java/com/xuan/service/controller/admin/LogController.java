package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.log.OperLogQueryDTO;
import com.xuan.entity.vo.log.OperLogVO;
import com.xuan.service.service.IOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志管理控制器
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/admin/log")
@RequiredArgsConstructor
public class LogController {

    private final IOperLogService operLogService;

    @Operation(summary = "查看操作日志")
    @GetMapping("/operation")
    public Result<Page<OperLogVO>> pageOperLogs(OperLogQueryDTO queryDTO) {
        return Result.success(operLogService.pageOperLogs(queryDTO));
    }
}
