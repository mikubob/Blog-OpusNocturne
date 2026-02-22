package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统设置表实体类
 * 对应数据库表：sys_setting
 * 用于存储站点配置信息
 * 
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_setting")
@Schema(description = "系统设置表实体类")
public class SysSetting extends BaseEntity {

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
     * 评论是否需要审核：1-是；0-否
     */
    @Schema(description = "评论是否需要审核：1-是；0-否", example = "1")
    private Integer commentAudit;

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

    /**
     * 关于我
     */
    @Schema(description = "关于我")
    private String aboutMe;

}