package com.xuan.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体共有的字段
 * 
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
@Schema(description = "基础实体类")
public abstract class BaseEntity implements Serializable {

    /**
     * 序列化ID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用自增策略
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    /**
     * 创建时间
     * 插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 插入和更新时自动填充
     */

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
