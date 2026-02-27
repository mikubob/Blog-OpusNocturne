package com.xuan.service.controller.blog;

import com.xuan.common.domain.Result;
import com.xuan.entity.dto.comment.CommentCreateDTO;
import com.xuan.entity.vo.comment.CommentPageVO;
import com.xuan.service.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 */
@Tag(name = "前台评论")
@RestController("blogCommentController")
@RequestMapping("/api/blog/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 分页获取文章评论树
     * <p>
     * 采用两级分页策略，外层分页顶级评论，内层批量查询子评论，避免大数据量 OOM。
     *
     * @param articleId 文章ID（0 表示留言板）
     * @param current   当前页码，默认 1
     * @param size      每页顶级评论数，默认 10
     */
    @Operation(summary = "分页获取文章评论树")
    @GetMapping("/tree/{articleId}")
    public Result<CommentPageVO> getCommentTree(
            @PathVariable Long articleId,
            @Parameter(description = "当前页码，从1开始") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页顶级评论数") @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getCommentTree(articleId, current, size));
    }

    @Operation(summary = "获取文章评论统计")
    @GetMapping("/stats/{articleId}")
    public Result<java.util.Map<String, Long>> getCommentStats(@PathVariable Long articleId) {
        return Result.success(commentService.getArticleCommentStats(articleId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Void> createComment(@Validated @RequestBody CommentCreateDTO dto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        commentService.createComment(dto, ip, userAgent);
        return Result.success();
    }
}
