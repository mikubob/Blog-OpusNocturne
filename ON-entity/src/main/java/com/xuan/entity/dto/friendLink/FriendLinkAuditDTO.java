package com.xuan.entity.dto.friendLink;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 友链审核 DTO
 * 对应接口：12.3.2 审核友链
 * @author 玄〤
 */
@Data
@Schema(description = "友链审核参数")
public class FriendLinkAuditDTO implements Serializable {

    @Schema(description = "审核状态：1-上线；2-下架/拒绝", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer status;
}
