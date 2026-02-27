package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.system.AttachmentPageQueryDTO;
import com.xuan.entity.po.interact.Attachment;
import com.xuan.service.service.IAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件管理控制器
 *
 * @author 玄〤
 */
@Tag(name = "附件管理")
@RestController("adminAttachmentController")
@RequestMapping("/api/admin/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final IAttachmentService attachmentService;

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<Attachment> uploadAttachment(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型，如 article、avatar") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "关联业务ID") @RequestParam(value = "bizId", required = false) Long bizId) {
        return Result.success(attachmentService.uploadAttachment(file, bizType, bizId));
    }

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<Page<Attachment>> pageAttachment(@Validated AttachmentPageQueryDTO dto) {
        return Result.success(attachmentService.pageAttachment(dto));
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return Result.success();
    }

    @Operation(summary = "批量删除附件")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteAttachment(@RequestBody List<Long> ids) {
        attachmentService.batchDeleteAttachment(ids);
        return Result.success();
    }
}
