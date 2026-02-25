package com.xuan.entity.dto.friendLink;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * 友链申请/新增 DTO/修改友链
 * 对应接口：13.3.3 修改友链 13.1 申请友情链接 (Portal)
 * @author 玄〤
 */
@Data
@Schema(description = "友链申请/新增参数/修改友链")
public class FriendLinkApplyAndUpdateDTO implements Serializable {

    @Schema(description = "网站名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "My Blog")
    @NotBlank(message = "网站名称不能为空")
    private String name;

    @Schema(description = "网站地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com")
    @NotBlank(message = "网站地址不能为空")
    @URL(message = "网站地址格式不正确")
    private String url;

    @Schema(description = "网站图标", example = "https://example.com/favicon.ico")
    private String icon;

    @Schema(description = "描述", example = "技术分享")
    private String description;

    @Schema(description = "站长邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
