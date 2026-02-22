package com.xuan.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型枚举
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Getter
@AllArgsConstructor
public enum PermissionTypeEnum {

    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    private final Integer code;
    private final String desc;

    public static PermissionTypeEnum fromCode(Integer code) {
        for (PermissionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的权限类型: " + code);
    }
}