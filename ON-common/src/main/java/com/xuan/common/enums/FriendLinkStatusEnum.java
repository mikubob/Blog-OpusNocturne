package com.xuan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 友链状态枚举
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Getter
@AllArgsConstructor
public enum FriendLinkStatusEnum {
    // 0-待审核
    PENDING(0, "待审核"),
    // 1-上线
    ONLINE(1, "上线"),
    // 2-下架
    OFFLINE(2, "下架");

    private final Integer code;
    private final String desc;

    public static FriendLinkStatusEnum fromCode(int code) {
        for (FriendLinkStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的友链审核状态："+code);
    }
}