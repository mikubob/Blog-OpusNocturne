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

    private final int code;
    private final String desc;
    public static FriendLinkStatusEnum fromCode(int code) {
        for (FriendLinkStatusEnum value : FriendLinkStatusEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}