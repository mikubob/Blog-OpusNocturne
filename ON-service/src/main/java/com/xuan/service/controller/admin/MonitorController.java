package com.xuan.service.controller.admin;

import com.xuan.common.domain.Result;
import com.xuan.entity.vo.monitor.ServerMonitorVO;
import com.xuan.service.service.IMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控控制器
 */
@Tag(name = "监控管理")
@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final IMonitorService monitorService;

    @Operation(summary = "获取服务器监控信息")
    @GetMapping("/server")
    public Result<ServerMonitorVO> getServerInfo() {
        return Result.success(monitorService.getServerInfo());
    }
}
