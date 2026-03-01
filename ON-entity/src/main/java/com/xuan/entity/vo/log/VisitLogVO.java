package com.xuan.entity.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访问日志返回VO类
 * 用于返回访问日志信息给前端
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Data
@Schema(description = "访问日志返回VO类")
public class VisitLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @Schema(description = "日志ID", example = "1")
    private Long id;

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
    @Schema(description = "访问时间", example = "2023-10-01 10:00:00")
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
