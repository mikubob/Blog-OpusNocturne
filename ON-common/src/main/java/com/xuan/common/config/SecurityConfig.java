package com.xuan.common.config;

import com.xuan.common.handle.AccessDeniedHandlerImpl;
import com.xuan.common.handle.AuthenticationEntryPointImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * 仅负责 Security 过滤链配置，JWT 拦截器注册由 WebConfig 负责
 *
 * @author 玄〤
 * @since 2026-02-18
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    /**
     * 创建安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF（使用 JWT，不需要 CSRF 保护）
        http.csrf(AbstractHttpConfigurer::disable);

        // 配置会话管理为无状态
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 配置异常处理
        http.exceptionHandling(handler -> handler
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        // 配置请求路径放行规则
        // 当前阶段先全部放行，后续开启细粒度控制
        http.authorizeHttpRequests(auth -> auth
                // TODO 开放阶段，先全部放行。后续按以下规则开启权限控制：
                // .requestMatchers("/api/admin/auth/login").permitAll()
                // .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**",
                // "/swagger-resources/**").permitAll()
                // .requestMatchers("/api/blog/**").permitAll()
                // .anyRequest().authenticated()
                .anyRequest().permitAll());

        return http.build();
    }
}
