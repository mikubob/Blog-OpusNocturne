package com.xuan.entity.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户列表响应数据类
 * 对应接口：4.1 分页获取用户列表响应
 * 用于返回用户列表数据
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "用户列表响应数据类")
public class UserListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "玄〤")
    private String nickname;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://cdn.jsdelivr.net/gh/xuan-xuan/blog-images/avatar.png")
    private String avatar;
    
    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-16T10:00:00")
    private LocalDateTime createTime;
}