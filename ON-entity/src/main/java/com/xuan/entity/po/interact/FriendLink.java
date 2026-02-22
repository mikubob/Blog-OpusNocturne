package com.xuan.entity.po.interact;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xuan.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 友情链接实体类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend_link")
@Schema(description = "友情链接实体类")
public class FriendLink extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "网站名称", example = "张三的主页")
    private String name;

    @Schema(description = "网站地址", example = "https://zhangsan.com")
    private String url;

    @Schema(description = "网站图标", example = "https://zhangsan.com/favicon.ico")
    private String icon;

    @Schema(description = "描述", example = "一个专注于Java技术的博客")
    private String description;

    @Schema(description = "站长邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "状态：0-待审核；1-上线；2-下架", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
