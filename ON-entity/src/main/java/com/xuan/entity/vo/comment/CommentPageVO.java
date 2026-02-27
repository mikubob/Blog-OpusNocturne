package com.xuan.entity.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页评论树响应数据类
 * <p>
 * 替代原先一次性返回全量评论树的方案，改为：
 * 外层分页顶级评论 + 每条顶级评论下的子评论列表（不再无限嵌套）。
 *
 * @author 玄〤
 * @since 2026-02-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页评论树响应数据类")
public class CommentPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 顶级评论总数（用于前端计算总页数） */
    @Schema(description = "顶级评论总数", example = "128")
    private Long total;

    /** 当前页的顶级评论列表（每条内部包含其子评论） */
    @Schema(description = "当前页评论列表")
    private List<CommentTreeVO> list;
}
