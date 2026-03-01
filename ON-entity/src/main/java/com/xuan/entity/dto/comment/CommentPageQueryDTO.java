package com.xuan.entity.dto.comment;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 评论分页查询请求参数类
 * 对应接口：7.5.1 分页获取评论列表
 * 用于接收前端分页查询评论的参数
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论分页查询请求参数类")
public class CommentPageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 按文章ID筛选
     */
    @Schema(description = "按文章ID筛选", example = "100")
    private Long articleId;

    /**
     * 按状态筛选：0-待审核，1-审核通过，2-审核未通过
     */
    @Schema(description = "按状态筛选：0-待审核，1-审核通过，2-审核未通过", example = "0")
    private Integer status;

    /**
     * 按昵称搜索
     */
    @Size(max = 50, message = "昵称搜索长度不能超过50个字符")
    @Schema(description = "按昵称搜索", example = "用户")
    private String nickname;
}
