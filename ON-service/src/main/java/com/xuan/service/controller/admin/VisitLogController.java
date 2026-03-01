package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.log.VisitLogQueryDTO;
import com.xuan.entity.vo.log.VisitLogVO;
import com.xuan.service.service.IVisitLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访问日志管理控制器
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Tag(name = "访问日志管理")
@RestController
@RequestMapping("/api/admin/log/visit")
@RequiredArgsConstructor
public class VisitLogController {

    private final IVisitLogService visitLogService;

    @Operation(summary = "分页查询访问日志")
    @GetMapping
    public Result<Page<VisitLogVO>> pageVisitLogs(VisitLogQueryDTO queryDTO) {
        // 确保分页参数不为null，提供默认值
        if (queryDTO.getCurrent() == null) {
            queryDTO.setCurrent(1);
        }
        if (queryDTO.getSize() == null) {
            queryDTO.setSize(10);
        }
        
        return Result.success(visitLogService.pageVisitLogs(queryDTO));
    }
}
