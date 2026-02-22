package com.xuan.common.constant;

/**
 * 时间格式常量类
 * 集中管理所有时间格式配置，防止直接硬编码在各个模块中
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public final class DateTimeFormatConstant { //将类设置为final，作用是防止继承

    /** 日期时间格式 */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /** 时间格式 */
    public static final String TIME_PATTERN = "HH:mm:ss";

    // 私有构造函数，防止实例化
    private DateTimeFormatConstant() {}
}
