package com.xuan.common.utils;

import java.util.concurrent.TimeUnit;

/**
 * 服务器监控信息工具类
 *
 * <h3>职责说明</h3>
 * <p>
 * 本工具类专注于服务器监控相关的通用数据处理逻辑，
 * 与具体的采集框架（如 OSHI）解耦，可被任意模块复用。
 * </p>
 *
 * <h3>包含功能</h3>
 * <ul>
 * <li>系统运行时长（uptime）的秒数 → 人类可读中文字符串格式化</li>
 * </ul>
 *
 * @author 玄〤
 * @since 2026-02-28
 */
public class ServerMonitorUtils {

    private ServerMonitorUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将系统运行时长（秒数）格式化为直观的中文时间描述字符串。
     *
     * <h4>格式规则</h4>
     * <ul>
     * <li>不足 60 秒：仅显示秒，如 {@code "45秒"}</li>
     * <li>超过 60 秒：按需组合天/小时/分钟，如 {@code "3天 2小时 15分钟"}</li>
     * <li>若天数为 0，则不显示天；若小时数为 0，则不显示小时；
     * 若分钟数为 0 但天/小时均有值，则不显示分钟（末尾修剪）；
     * 但若天和小时均为 0，则强制显示 "0分钟"（通过 {@code sb.length() == 0} 判断兜底）。</li>
     * </ul>
     *
     * <h4>实现技巧</h4>
     * <p>
     * {@link TimeUnit#SECONDS#toDays(long)} 等工具方法内部进行的是整除运算，
     * 配合取模运算（{@code %}）可以拆分出各时间单位的余数部分，避免手动处理进位逻辑。
     * </p>
     *
     * @param seconds 系统运行时长，单位：秒，非负
     * @return 人类可读的运行时长字符串，如 {@code "1天 3小时 20分钟"} 或 {@code "30秒"}
     */
    public static String formatUptime(long seconds) {
        // 特殊情况：系统运行不足 1 分钟，直接用秒展示
        if (seconds < 60) {
            return seconds + "秒";
        }

        // 利用 TimeUnit 的换算方法提取各时间单位
        long days = TimeUnit.SECONDS.toDays(seconds); // 完整天数
        long hours = TimeUnit.SECONDS.toHours(seconds) % 24; // 去掉完整天后的剩余小时数
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60; // 去掉完整小时后的剩余分钟数

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("天 ");
        if (hours > 0)
            sb.append(hours).append("小时 ");
        // 当分钟数大于 0 时追加；若前面什么都没追加（days=0, hours=0），则兜底追加 "0分钟"
        if (minutes > 0 || sb.length() == 0)
            sb.append(minutes).append("分钟");

        return sb.toString().trim(); // 去除末尾可能残留的空格
    }
}
