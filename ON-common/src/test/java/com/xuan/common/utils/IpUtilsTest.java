package com.xuan.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IpUtils测试类
 * 测试IP地址解析功能
 */
public class IpUtilsTest {

    @Test
    public void testGetIpLocation() {
        // 测试本地IP
        String localIp = "127.0.0.1";
        String localLocation = IpUtils.getIpLocation(localIp);
        System.out.println("本地IP: " + localIp + " -> 位置: " + localLocation);
        assertEquals("本地", localLocation);

        // 测试IPv6本地IP
        String localIpv6 = "0:0:0:0:0:0:0:1";
        String localLocationIpv6 = IpUtils.getIpLocation(localIpv6);
        System.out.println("IPv6本地IP: " + localIpv6 + " -> 位置: " + localLocationIpv6);
        assertEquals("本地", localLocationIpv6);

        // 测试未知IP
        String unknownIp = "unknown";
        String unknownLocation = IpUtils.getIpLocation(unknownIp);
        System.out.println("未知IP: " + unknownIp + " -> 位置: " + unknownLocation);
        assertEquals("未知", unknownLocation);

        // 测试空IP
        String emptyIp = "";
        String emptyLocation = IpUtils.getIpLocation(emptyIp);
        System.out.println("空IP: " + emptyIp + " -> 位置: " + emptyLocation);
        assertEquals("未知", emptyLocation);

        // 测试null IP
        String nullIp = null;
        String nullLocation = IpUtils.getIpLocation(nullIp);
        System.out.println("null IP: " + nullIp + " -> 位置: " + nullLocation);
        assertEquals("未知", nullLocation);

        // 测试国内IP（114DNS）
        String chinaIp = "114.114.114.114";
        String chinaLocation = IpUtils.getIpLocation(chinaIp);
        System.out.println("国内IP: " + chinaIp + " -> 位置: " + chinaLocation);
        assertNotNull(chinaLocation);

        // 测试局域网IP
        String lanIp = "192.168.1.1";
        String lanLocation = IpUtils.getIpLocation(lanIp);
        System.out.println("局域网IP: " + lanIp + " -> 位置: " + lanLocation);
        assertNotNull(lanLocation);

        // 测试Google DNS（可能返回未知）
        String googleIp = "8.8.8.8";
        String googleLocation = IpUtils.getIpLocation(googleIp);
        System.out.println("Google DNS: " + googleIp + " -> 位置: " + googleLocation);
        assertNotNull(googleLocation);

        // 测试百度IP
        String baiduIp = "180.101.50.188";
        String baiduLocation = IpUtils.getIpLocation(baiduIp);
        System.out.println("百度IP: " + baiduIp + " -> 位置: " + baiduLocation);
        assertNotNull(baiduLocation);

        System.out.println("IP解析测试完成！");
    }
}
