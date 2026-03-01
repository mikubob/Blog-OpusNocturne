package com.xuan.entity.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 操作日志查询参数类
 * 对应接口：14.2 查看操作日志
 * 用于接收前端查询操作日志的参数
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Data
@Schema(description = "操作日志查询参数类")
public class OperLogQueryDTO implements Serializable {

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
     * 模块名称
     */
    @Schema(description = "模块名称", example = "文章管理")
    private String module;

    /**
     * 状态：1-成功；0-失败
     */
    @Schema(description = "状态：1-成功；0-失败", example = "1")
    private Integer status;

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
}
