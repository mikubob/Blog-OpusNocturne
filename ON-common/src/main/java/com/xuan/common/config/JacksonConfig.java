package com.xuan.common.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.xuan.common.utils.DateTimeFormatUtils;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局序列化配置
 * <p>
 * 作用：
 * 1. 统一 Java 8 时间类型（LocalDateTime/LocalDate/LocalTime）的序列化和反序列化格式
 * 2. 使 HTTP 响应中的时间格式与 application.yaml 中的 date-format 保持一致
 * 3. 解决 spring.jackson.date-format 只对 java.util.Date 生效、不影响 LocalDateTime 的问题
 * <p>
 * 格式约定：
 * - LocalDateTime → yyyy-MM-dd HH:mm:ss
 * - LocalDate → yyyy-MM-dd
 * - LocalTime → HH:mm:ss
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 Jackson ObjectMapper 构建器
     * <p>
     * 通过 Jackson2ObjectMapperBuilderCustomizer 注入自定义的 JavaTimeModule，
     * Spring Boot 会自动将其应用到全局 ObjectMapper 中（包括 Spring MVC 的 HTTP 序列化）。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {

            // 序列化器（Java 对象 → JSON 字符串）
            builder.serializers(
                    new LocalDateTimeSerializer(DateTimeFormatUtils.DATETIME_FORMATTER),
                    new LocalDateSerializer(DateTimeFormatUtils.DATE_FORMATTER),
                    new LocalTimeSerializer(DateTimeFormatUtils.TIME_FORMATTER));

            // 反序列化器（JSON 字符串 → Java 对象）
            builder.deserializers(
                    new LocalDateTimeDeserializer(DateTimeFormatUtils.DATETIME_FORMATTER),
                    new LocalDateDeserializer(DateTimeFormatUtils.DATE_FORMATTER),
                    new LocalTimeDeserializer(DateTimeFormatUtils.TIME_FORMATTER));
        };
    }
}
