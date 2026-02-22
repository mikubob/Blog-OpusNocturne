package com.xuan.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * knife4j配置类 - OpenAPI3.0
 * <p>
 * 作用：
 * 1. 配置OpenAPI基本信息，用于Knife4j接口文档生成
 * 2. 支持通过application.yml文件进行类型安全的配置管理
 * 3. 按照Spring Boot 3.4.2配置规范进行自动配置管理
 * <p>
 * 配置属性：
 * - knife4j.title: API文档标题
 * - knife4j.description: API文档描述
 * - knife4j.version: API文档版本
 * - knife4j.contact.name: 联系人姓名
 * - knife4j.contact.email: 联系人邮箱
 * - knife4j.contact.url: 联系人URL
 * <p>
 * @author 玄〤
 * @since 2026-02-17
 */
@Configuration
public class Knife4jConfig {

    /**
     * OpenAPI基本信息配置
     */
    @Bean
    public OpenAPI customOpenAPI(Knife4jProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContact().getName())
                                .email(properties.getContact().getEmail())
                                .url(properties.getContact().getUrl())));
    }

    /**
     * knife4j配置属性类
     * @return knife4j配置属性类
     */
    @Bean
    @ConfigurationProperties(prefix = "knife4j")
    public Knife4jProperties knife4jProperties() {
        return new Knife4jProperties();
    }

    /**
     * Knife4j配置属性类
     */
    @Data
    public static class Knife4jProperties {
        private String title = "OpusNocturne博客系统API文档";
        private String description = "基于Spring Boot 3.4.2 和 JDK21的现代化博客系统";
        private String version = "1.0.0";
        private ContactProperties contact = new ContactProperties();

        /**
         * 联系人配置属性
         */
        @Data
        public static class ContactProperties {
            private String name = "玄〤";
            private String email = "2386782347@qq.com";
            private String url = "https://github.com/mikubob/Blog-OpusNocturne";

        }
    }

}
