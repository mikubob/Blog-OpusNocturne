package com.xuan.entity.dto.user;

import com.xuan.common.domain.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户列表分页查询参数类
 * 对应接口：4.1 分页获取用户列表
 * 用于接收前端分页查询用户列表的参数
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户列表分页查询参数类")
public class UserPageQueryDTO extends BasePageQueryDTO {
    /**
     * 用户名搜索
     */
    @Size(max = 50, message = "用户名搜索长度不能超过50个字符")
    @Schema(description = "用户名搜索", example = "admin")
    private String username;
    
    /**
     * 昵称搜索
     */
    @Size(max = 50, message = "昵称搜索长度不能超过50个字符")
    @Schema(description = "昵称搜索", example = "管理员")
    private String nickname;
}