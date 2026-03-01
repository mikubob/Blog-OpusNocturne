package com.xuan.entity.vo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器监控信息 VO
 * 对应接口：14.5 获取服务器监控信息 (Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务器监控信息")
public class ServerMonitorVO {

    @Schema(description = "CPU信息")
    private CpuVO cpu;

    @Schema(description = "内存信息")
    private MemoryVO memory;

    @Schema(description = "系统信息")
    private SystemVO system;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "CPU信息")
    public static class CpuVO {
        @Schema(description = "CPU名称")
        private String name;
        @Schema(description = "物理核心数")
        private Integer packages;
        @Schema(description = "逻辑核心数")
        private Integer cores;
        @Schema(description = "CPU使用率")
        private Double usage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "内存信息")
    public static class MemoryVO {
        @Schema(description = "内存总量")
        private String total;
        @Schema(description = "已用内存")
        private String used;
        @Schema(description = "剩余内存")
        private String free;
        @Schema(description = "内存使用率")
        private Double usage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "系统信息")
    public static class SystemVO {
        @Schema(description = "操作系统")
        private String os;
        @Schema(description = "系统架构")
        private String arch;
        @Schema(description = "运行时间")
        private String uptime;
    }
}
