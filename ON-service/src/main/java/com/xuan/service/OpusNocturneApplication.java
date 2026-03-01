package com.xuan.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OpusNocturne博客系统启动类
 *
 * @author 玄〤
 */
@SpringBootApplication(scanBasePackages = "com.xuan")
@MapperScan("com.xuan.service.mapper")
@EnableScheduling // 开启定时任务功能
@EnableAsync // 开启异步功能
public class OpusNocturneApplication {

    // ANSI 颜色代码常量
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";

    public static void main(String[] args) {
        SpringApplication.run(OpusNocturneApplication.class, args);
        printBanner();
    }

    private static void printBanner() {
        System.out.println(BRIGHT_CYAN + "\n" +
                "╔═══════════════════════════════════════════════════════════════════════════╗\n" +
                "║                                                                           ║\n" +
                "║                    " + BRIGHT_MAGENTA + BOLD + " ██████╗ ██████╗ ██╗   ██╗███████╗ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                    " + MAGENTA + BOLD +        "██╔═══██╗██╔══██╗██║   ██║██╔════╝ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                    " + RED + BOLD +            "██║   ██║██████╔╝██║   ██║███████╗ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                    " + YELLOW + BOLD +         "██║   ██║██╔═══╝ ██║   ██║╚════██║ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                    " + GREEN + BOLD +          "╚██████╔╝██║     ╚██████╔╝███████║ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                    " + CYAN + BOLD +           " ╚═════╝ ╚═╝      ╚═════╝ ╚══════╝ " + RESET + BRIGHT_CYAN + "                    ║\n" +
                "║                                                                           ║\n" +
                "║ " + BRIGHT_CYAN + BOLD +   "███╗   ██╗ ██████╗  ██████╗ ████████╗██╗   ██╗██████╗ ███╗   ██╗███████╗" + RESET + BRIGHT_CYAN + "  ║\n" +
                "║ " + BLUE + BOLD +          "████╗  ██║██╔═══██╗██╔════╝ ╚══██╔══╝██║   ██║██╔══██╗████╗  ██║██╔════╝" + RESET + BRIGHT_CYAN + "  ║\n" +
                "║ " + BRIGHT_BLUE + BOLD +   "██╔██╗ ██║██║   ██║██║         ██║   ██║   ██║██████╔╝██╔██╗ ██║█████╗  " + RESET + BRIGHT_CYAN + "  ║\n" +
                "║ " + MAGENTA + BOLD +       "██║╚██╗██║██║   ██║██║         ██║   ██║   ██║██╔══██╗██║╚██╗██║██╔══╝  " + RESET + BRIGHT_CYAN + "  ║\n" +
                "║ " + BRIGHT_MAGENTA + BOLD + "██║ ╚████║╚██████╔╝╚██████╗    ██║   ╚██████╔╝██║  ██║██║ ╚████║███████╗" + RESET + BRIGHT_CYAN + "  ║\n" +
                "║ " + RED + BOLD +           "╚═╝  ╚═══╝ ╚═════╝  ╚═════╝    ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝" + RESET + BRIGHT_CYAN + "  ║\n" +
                "║                                                                           ║\n" +
                "╠═══════════════════════════════════════════════════════════════════════════╣\n" +
                "║                                                                           ║\n" +
                "║                 " + WHITE + BOLD + "█▄▄ █░░ █▀█ █▀▀   █▀ █▄█ █▀ ▀█▀ █▀▀ █▀▄▀█" + RESET + BRIGHT_CYAN + "                 ║\n" +
                "║                 " + WHITE + BOLD + "█▄█ █▄▄ █▄█ █▄█   ▄█ ░█░ ▄█ ░█░ ██▄ █░▀░█" + RESET + BRIGHT_CYAN + "                 ║\n" +
                "║                                                                           ║\n" +
                "╠═══════════════════════════════════════════════════════════════════════════╣\n" +
                "║                                                                           ║\n" +
                "║    " + YELLOW + BOLD + "📌 OPUSNOCTURNE 博客系统启动成功!" + RESET + BRIGHT_CYAN + "                                        ║\n" +
                "║                                                                           ║\n" +
                "║    " + GREEN + "🌐 API 文档地址:" + RESET + WHITE + " http://localhost:8080/doc.html" + RESET + BRIGHT_CYAN + "                         ║\n" +
                "║    " + BLUE + "👤 作者:" + RESET + WHITE + " 玄〤" + RESET + BRIGHT_CYAN + "                                                           ║\n" +
                "║    " + MAGENTA + "📦 版本:" + RESET + WHITE + " 1.0.0" + RESET + BRIGHT_CYAN + "                                                         ║\n" +
                "║                                                                           ║\n" +
                "╚═══════════════════════════════════════════════════════════════════════════╝\n" +
                RESET);
    }
}