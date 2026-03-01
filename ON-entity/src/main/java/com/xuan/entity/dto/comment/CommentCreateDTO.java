package com.xuan.entity.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发表评论请求参数类
 * 对应接口：7.4 发表评论
 * 用于接收前端发表评论的参数
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "发表评论请求参数类")
public class CommentCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID，必填，留言板传 0
     */
    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID，留言板传 0", example = "100", requiredMode = RequiredMode.REQUIRED)
    private Long articleId;

    /**
     * 评论内容，必填
     */
    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", example = "写的真好！", requiredMode = RequiredMode.REQUIRED)
    private String content;

    /**
     * 父评论ID，回复时必填
     */
    @Schema(description = "父评论ID，回复时必填", example = "501")
    private Long parentId;

    /**
     * 根评论ID，回复楼中楼时必填
     */
    @Schema(description = "根评论ID，回复楼中楼时必填", example = "501")
    private Long rootParentId;

    /**
     * 昵称，必填
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50字符")
    @Schema(description = "昵称", example = "用户A", requiredMode = RequiredMode.REQUIRED)
    private String nickname;

    /**
     * 邮箱
     */
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    /**
     * 个人网站地址
     */
    @Size(max = 200, message = "网站地址长度不能超过200字符")
    @Schema(description = "个人网站地址", example = "https://example.com")
    private String website;
}
