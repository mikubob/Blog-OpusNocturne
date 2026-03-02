package com.xuan.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * XSS 防护过滤器
 * <p>
 * 对所有请求的参数和请求体进行 XSS 过滤，将危险的 HTML 标签和脚本进行转义。
 * 排除 Knife4j 文档和静态资源路径。
 * <p>
 * 过滤规则：
 * - 转义 HTML 实体字符：&, <, >, ", '
 * - 移除 javascript:、vbscript: 等危险协议
 * - 移除 onXxx 事件属性（如 onclick, onerror 等）
 * - 移除 <script> 标签
 *
 * @author 玄〤
 * @since 2026-03-02
 */
@Slf4j
@Component
@Order(1)
public class XssFilter implements Filter {

    /** 不需要 XSS 过滤的路径 */
    private static final String[] EXCLUDE_PATHS = {
            "/doc.html", "/webjars/", "/v3/api-docs/",
            "/swagger-resources/", "/uploads/", "/favicon.ico"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        // 排除不需要过滤的路径
        for (String excludePath : EXCLUDE_PATHS) {
            if (uri.startsWith(excludePath)) {
                chain.doFilter(request, response);
                return;
            }
        }

        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

    /**
     * XSS 请求包装器
     * 对请求参数和请求体中的内容进行 XSS 过滤
     */
    public static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        /**
         * 过滤单个请求参数值
         */
        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? cleanXss(value) : null;
        }

        /**
         * 过滤请求参数值数组
         */
        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] cleanValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleanValues[i] = cleanXss(values[i]);
            }
            return cleanValues;
        }

        /**
         * 过滤请求头
         */
        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return value != null ? cleanXss(value) : null;
        }

        /**
         * 过滤请求体（JSON Body）
         */
        @Override
        public ServletInputStream getInputStream() throws IOException {
            // 读取原始请求体
            String body = new BufferedReader(new InputStreamReader(
                    super.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            // 如果请求体为空，直接返回
            if (body.isEmpty()) {
                return super.getInputStream();
            }

            // 对请求体进行 XSS 过滤（JSON Body 不做 HTML 实体转义，否则会破坏 JSON 格式）
            String cleanBody = cleanXssForJson(body);
            byte[] bytes = cleanBody.getBytes(StandardCharsets.UTF_8);

            return new ServletInputStream() {
                private final ByteArrayInputStream delegate = new ByteArrayInputStream(bytes);

                @Override
                public boolean isFinished() {
                    return delegate.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // 不需要异步支持
                }

                @Override
                public int read() {
                    return delegate.read();
                }
            };
        }
    }

    /**
     * XSS 清理方法（完整版，用于请求参数和请求头）
     * <p>
     * 对输入字符串进行 XSS 特征清理：
     * 1. 移除 <script> 标签及其内容
     * 2. 移除 javascript:、vbscript: 等危险协议
     * 3. 移除 onXxx 事件处理属性
     * 4. 转义 HTML 实体字符
     *
     * @param value 原始输入字符串
     * @return 过滤后的安全字符串
     */
    public static String cleanXss(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 先移除危险标签和脚本
        value = stripDangerousTags(value);

        // 转义 HTML 实体字符，防止 HTML 注入
        value = value.replace("&", "&amp;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\"", "&quot;");
        value = value.replace("'", "&#x27;");

        return value;
    }

    /**
     * XSS 清理方法（JSON Body 专用）
     * <p>
     * 只移除危险标签和脚本，不做 HTML 实体转义。
     * 因为 JSON Body 中的 " 和 & 等字符是 JSON 语法的一部分，
     * 转义会导致 JSON 格式被破坏，Jackson 无法解析。
     *
     * @param value 原始输入字符串
     * @return 过滤后的安全字符串
     */
    public static String cleanXssForJson(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return stripDangerousTags(value);
    }

    /**
     * 移除危险的 HTML 标签和脚本（公共逻辑）
     *
     * @param value 原始输入字符串
     * @return 移除危险标签后的字符串
     */
    private static String stripDangerousTags(String value) {
        // 移除 <script> 标签及其内容（不区分大小写）
        value = value.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        // 移除 <script> 自闭合标签
        value = value.replaceAll("(?i)<script[^>]*/>", "");
        // 移除 <script> 开标签（防止未闭合的情况）
        value = value.replaceAll("(?i)<script[^>]*>", "");

        // 移除 javascript: 和 vbscript: 协议
        value = value.replaceAll("(?i)javascript\\s*:", "");
        value = value.replaceAll("(?i)vbscript\\s*:", "");

        // 移除 onXxx 事件属性（如 onclick, onerror, onload 等）
        value = value.replaceAll("(?i)\\bon\\w+\\s*=", "");

        return value;
    }
}
