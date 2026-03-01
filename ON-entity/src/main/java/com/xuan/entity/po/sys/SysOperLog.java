package com.xuan.entity.po.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_oper_log")
@Schema(description = "操作日志实体类")
public class SysOperLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键id",example = "1")
    private Long id;

    /** 模块标题 */
    @Schema(description = "模块标题",example = "系统管理")
    private String module;

    /** 业务类型（0其它 1新增 2修改 3删除） */
    @Schema(description = "业务类型（0其它 1新增 2修改 3删除）",example = "1")
    private String businessType;

    /** 方法名称 */
    @Schema(description = "方法名称",example = "add")
    private String method;

    /** 请求方式 */
    @Schema(description = "请求方式",example = "POST")
    private String requestMethod;

    /** 操作类别（0其它 1后台用户 2手机端用户） */
    @Schema(description = "操作类别（0其它 1后台用户 2手机端用户）",example = "1")
    private String operatorType;

    /** 操作人员 */
    @Schema(description = "操作人员",example = "admin")
    private String operName;

    /** 请求URL */
    @Schema(description = "请求URL",example = "http://localhost:8080/api/v1/sys/operLog")
    private String operUrl;

    /** 主机地址 */
    @Schema(description = "主机地址",example = "127.0.0.1")
    private String operIp;

    /** 操作地点 */
    @Schema(description = "操作地点",example = "中国")
    private String operLocation;

    /** 请求参数 */
    @Schema(description = "请求参数",example = "{\"name\":\"admin\"}")
    private String operParam;

    /** 返回参数 */
    @Schema(description = "返回参数",example = "{\"code\":200,\"message\":\"操作成功\"}")
    private String jsonResult;

    /** 操作状态（1正常 0异常） */
    @Schema(description = "操作状态（1正常 0异常）",example = "1")
    private Integer status;

    /** 错误消息 */
    @Schema(description = "错误消息",example = "操作失败")
    private String errorMsg;

    /** 操作时间 */
    @Schema(description = "操作时间",example = "2026-02-20 10:00:00")
    private LocalDateTime operTime;

    /** 消耗时间 */
    @Schema(description = "消耗时间",example = "100")
    private Long costTime;
}
