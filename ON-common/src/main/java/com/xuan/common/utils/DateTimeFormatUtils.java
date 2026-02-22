package com.xuan.common.utils;

import java.time.format.DateTimeFormatter;

import static com.xuan.common.constant.DateTimeFormatConstant.DATETIME_PATTERN;
import static com.xuan.common.constant.DateTimeFormatConstant.DATE_PATTERN;
import static com.xuan.common.constant.DateTimeFormatConstant.TIME_PATTERN;

/**
 * 日期时间格式统一配置
 * <p>
 * 作用：
 * 1. 统一管理项目中所有时间格式常量
 * 2. 确保 HTTP API、Redis 缓存等场景的时间格式保持一致
 * 3. 避免在多个配置类中重复定义相同的格式字符串
 *
 * @author 玄〤
 * @since 2026-02-21
 */
public class DateTimeFormatUtils {



    /** 日期时间格式化器 */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    /** 日期格式化器 */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    /** 时间格式化器 */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private DateTimeFormatUtils() {
        // 私有构造函数，防止实例化
    }
}
