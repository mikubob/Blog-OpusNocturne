package com.xuan.entity.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志返回VO类
 * 对应接口：14.2 查看操作日志
 * 用于返回操作日志信息给前端
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Data
@Schema(description = "操作日志返回VO类")
public class OperLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @Schema(description = "日志ID", example = "1")
    private Long id;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称", example = "文章管理")
    private String module;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述", example = "发布文章")
    private String operation;

    /**
     * 操作人员
     */
    @Schema(description = "操作人员", example = "admin")
    private String operator;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "127.0.0.1")
    private String ip;

    /**
     * 操作状态（1正常 0异常）
     */
    @Schema(description = "操作状态（1正常 0异常）", example = "1")
    private Integer status;

    /**
     * 消耗时间
     */
    @Schema(description = "消耗时间", example = "50")
    private Long costTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-10-01 10:00:00")
    private LocalDateTime createTime;
}
