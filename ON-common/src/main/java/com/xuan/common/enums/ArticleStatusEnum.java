package com.xuan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态枚举
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "发布"),
    WITHDRAWN(2, "下架");

    private final Integer code;
    private final String desc;

    public static ArticleStatusEnum fromCode(int code) {
        for (ArticleStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的文章状态: " + code);
    }
}