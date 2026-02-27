package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.comment.CommentAuditDTO;
import com.xuan.entity.dto.comment.CommentBatchAuditDTO;
import com.xuan.entity.dto.comment.CommentPageQueryDTO;
import com.xuan.entity.vo.comment.CommentAdminVO;
import com.xuan.service.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台评论管理控制器
 */
@Tag(name = "后台评论管理")
@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @Operation(summary = "分页获取评论列表")
    @GetMapping("/page")
    public Result<Page<CommentAdminVO>> pageComments(@Validated CommentPageQueryDTO dto) {
        return Result.success(commentService.pageComments(dto));
    }

    @Operation(summary = "审核评论")
    @PutMapping("/{id}/audit")
    public Result<Void> auditComment(@PathVariable Long id, @Validated @RequestBody CommentAuditDTO dto) {
        commentService.auditComment(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @Operation(summary = "批量审核评论")
    @PutMapping("/batch-audit")
    public Result<Void> batchAuditComments(@Validated @RequestBody CommentBatchAuditDTO dto) {
        commentService.batchAuditComments(dto.getIds(), dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "批量删除评论")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDeleteComments(@RequestBody List<Long> ids) {
        commentService.batchDeleteComments(ids);
        return Result.success();
    }
}