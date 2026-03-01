package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 访问记录表实体类
 * 对应数据库表：visit_log
 * 用于存储站点访问记录
 * @author 玄〤
 * @since 2026-02-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visit_log")
@Schema(description = "访问记录表实体类")
public class VisitLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 访问时间
     */
    @Schema(description = "访问时间", example = "2026-02-17 10:00:00")
    private LocalDateTime visitTime;

    /**
     * 访问页面URL
     */
    @Schema(description = "访问页面URL", example = "/blog/article/1")
    private String pageUrl;

    /**
     * 来源URL
     */
    @Schema(description = "来源URL", example = "https://www.google.com")
    private String referer;

}