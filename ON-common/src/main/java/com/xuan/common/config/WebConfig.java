package com.xuan.common.config;

import com.xuan.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置拦截器、跨域、静态资源等
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

        private final JwtInterceptor jwtInterceptor;

        /** 文件存储根目录（对应 application.yaml 中的 file.upload.path） */
        @Value("${file.upload.path:./uploads}")
        private String uploadPath;

        /** 文件访问URL前缀（对应 application.yaml 中的 file.upload.url-prefix） */
        @Value("${file.upload.url-prefix:/uploads}")
        private String urlPrefix;

        /**
         * 注册拦截器
         * 后台管理 API 需要 JWT 认证，前台 API 不需要
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(jwtInterceptor)
                                .addPathPatterns("/api/admin/**") // 拦截所有后台接口
                                .excludePathPatterns(
                                                "/api/admin/auth/login", // 排除登录接口
                                                "/doc.html", // 排除 API 文档
                                                "/webjars/**",
                                                "/v3/api-docs/**",
                                                "/swagger-resources/**");
        }

        /**
         * 配置跨域
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);
        }

        /**
         * 配置静态资源访问路径
         * 上传目录路径和 URL 前缀均从配置文件读取，便于生产环境切换
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 上传文件访问路径：根据配置动态确定 URL 前缀和存储位置
                String pattern = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
                pattern = pattern.endsWith("/**") ? pattern : pattern + "/**";
                String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
                if (!location.startsWith("file:")) {
                        location = "file:" + location;
                }
                registry.addResourceHandler(pattern)
                                .addResourceLocations(location);

                // Swagger 文档访问路径
                registry.addResourceHandler("/doc.html")
                                .addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
}
