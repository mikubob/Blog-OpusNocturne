package com.xuan.entity.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统设置响应数据类
 * 对应接口：11.1 获取系统设置
 * 用于返回系统设置信息
 * 
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@Schema(description = "系统设置响应数据类")
public class SystemSettingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 站点名称
     */
    @Schema(description = "站点名称", example = "OpusNocturne")
    private String siteName;

    /**
     * 站点描述
     */
    @Schema(description = "站点描述", example = "个人技术博客")
    private String siteDescription;

    /**
     * 站点关键词
     */
    @Schema(description = "站点关键词", example = "Java,Spring Boot,前端")
    private String siteKeywords;

    /**
     * 页脚文本
     */
    @Schema(description = "页脚文本", example = "© 2026 OpusNocturne")
    private String footerText;

    /**
     * 管理员邮箱
     */
    @Schema(description = "管理员邮箱", example = "admin@example.com")
    private String adminEmail;

    /**
     * 评论是否需要审核
     */
    @Schema(description = "评论是否需要审核", example = "true")
    private Boolean commentAudit;

    /**
     * 文章列表每页条数
     */
    @Schema(description = "文章列表每页条数", example = "10")
    private Integer articlePageSize;

    /**
     * 评论列表每页条数
     */
    @Schema(description = "评论列表每页条数", example = "20")
    private Integer commentPageSize;

    @Schema(description = "关于我")
    private String aboutMe;

}