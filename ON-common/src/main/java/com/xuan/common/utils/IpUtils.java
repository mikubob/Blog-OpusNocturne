package com.xuan.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 * 
 * @author 玄〤
 */
public class IpUtils {

    private static Searcher searcher;

    static {
        try {
            // 从classpath加载ip2region.xdb文件
            InputStream inputStream = IpUtils.class.getClassLoader().getResourceAsStream("ip2region.xdb");
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                searcher = Searcher.newWithBuffer(bytes);
                inputStream.close();
            }
        } catch (Exception e) {
            // 静默处理异常，确保系统正常运行
        }
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    /**
     * 根据IP地址获取地理位置
     * @param ip IP地址
     * @return 地理位置，解析失败返回"未知"
     */
    public static String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        
        // 本地IP返回本地
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "本地";
        }
        
        if (searcher == null) {
            return "未知";
        }
        
        try {
            String region = searcher.search(ip);
            if (region != null) {
                // 格式：国家|区域|省份|城市|ISP
                String[] parts = region.split("\\|");
                StringBuilder location = new StringBuilder();
                
                // 只取省份和城市
                if (parts.length > 2 && !"0".equals(parts[2])) {
                    location.append(parts[2]);
                }
                if (parts.length > 3 && !"0".equals(parts[3])) {
                    if (location.length() > 0) {
                        location.append(" ");
                    }
                    location.append(parts[3]);
                }
                
                return location.length() > 0 ? location.toString() : "未知";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "未知";
    }
}
