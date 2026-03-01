package com.xuan.entity.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论树响应数据类
 * 对应接口：7.1 获取文章评论树
 * 用于返回评论的树形结构数据
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "评论树响应数据类")
public class CommentTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 评论ID
     */
    @Schema(description = "评论ID", example = "1")
    private Long id;
    
    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "用户A")
    private String nickname;
    
    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "写的真好！")
    private String content;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-16T10:00:00")
    private LocalDateTime createTime;
    
    /**
     * 被回复人昵称
     */
    @Schema(description = "被回复人昵称", example = "用户B")
    private String replyNickname;
    
    /**
     * 子评论列表（仅显示部分）
     */
    @Schema(description = "子评论列表（仅显示部分）")
    private List<CommentTreeVO> children;

    /**
     * 子评论总数
     */
    @Schema(description = "子评论总数", example = "5")
    private Integer childCount;
}