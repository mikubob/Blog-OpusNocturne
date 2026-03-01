package com.xuan.entity.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论后台管理响应数据类
 * 对应接口：7.5.1 分页获取评论列表
 * 用于返回评论的详细信息
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "评论后台管理响应数据类")
public class CommentAdminVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 评论ID
     */
    @Schema(description = "评论ID", example = "1")
    private Long id;

    /**
     * 文章ID
     */
    @Schema(description = "文章ID", example = "100")
    private Long articleId;

    /**
     * 文章标题
     */
    @Schema(description = "文章标题", example = "Spring Boot 3实战")
    private String articleTitle;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "用户A")
    private String nickname;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "写的真好！")
    private String content;

    /**
     * 状态：0-待审核，1-审核通过，2-审核未通过
     */
    @Schema(description = "状态：0-待审核，1-审核通过，2-审核未通过", example = "1")
    private Integer status;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "127.0.0.1")
    private String ipAddress;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-17T10:00:00")
    private LocalDateTime createTime;
}
