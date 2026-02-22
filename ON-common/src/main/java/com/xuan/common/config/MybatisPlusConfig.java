package com.xuan.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * <p>
 * 作用：
 * 1. 配置MyBatis-Plus插件，如分页插件
 * 2. 按照Spring Boot 3.4.2配置规范进行自动配置管理
 * <p>
 * 配置内容：
 * - 分页插件：支持MySQL等数据库的分页查询
 * - 性能分析插件：可选，用于开发环境性能监控
 * - 乐观锁插件：可选，用于并发控制
 */
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor=new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));//配置多个插件，分页插件都需要放在最后面
        return interceptor;
    }
}
