package com.xuan.entity.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 访问日志查询参数类
 * 用于接收前端查询访问日志的参数
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Data
@Schema(description = "访问日志查询参数类")
public class VisitLogQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer current;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    private Integer size;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2023-10-01 00:00:00")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2023-10-02 00:00:00")
    private String endTime;

    /**
     * 访问页面URL
     */
    @Schema(description = "访问页面URL", example = "/blog/article/1")
    private String pageUrl;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "127.0.0.1")
    private String ipAddress;
}
