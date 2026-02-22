package com.xuan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Getter
@AllArgsConstructor
public enum CommentStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核未通过");

    private final Integer code;
    private final String desc;

    public static CommentStatusEnum fromCode(Integer code) {
        for (CommentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的评论状态: " + code);
    }
}