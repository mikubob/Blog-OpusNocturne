package com.xuan.entity.po.interact;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 评论表实体类
 * 对应数据库表：comment
 * 用于存储文章评论信息
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
@Schema(description = "评论表实体类")
public class Comment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章id(null为留言)
     */
    @Schema(description = "文章id(null为留言)", example = "100")
    private Long articleId;

    /**
     * 评论人id(游客为null)
     */
    @Schema(description = "评论人id(游客为null)", example = "1")
    private Long userId;

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
     * 内容
     */
    @Schema(description = "内容", example = "写的真好！")
    private String content;

    /**
     * 根评论id
     */
    @Schema(description = "根评论id", example = "1")
    private Long rootParentId;

    /**
     * 父评论id
     */
    @Schema(description = "父评论id", example = "1")
    private Long parentId;

    /**
     * 被回复人id
     */
    @Schema(description = "被回复人id", example = "2")
    private Long replyUserId;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "127.0.0.1")
    private String ipAddress;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息", example = "Mozilla/5.0...")
    private String userAgent;

    /**
     * 状态：0-待审核；1-审核通过；2-审核未通过
     */
    @Schema(description = "状态：0-待审核；1-审核通过；2-审核未通过", example = "1")
    private Integer status;
}