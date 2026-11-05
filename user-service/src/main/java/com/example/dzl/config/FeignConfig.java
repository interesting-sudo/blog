package com.example.dzl.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类
 * 用于为 Feign 请求自动添加内部服务认证头
 */
@Configuration
@Slf4j
public class FeignConfig {

    @Value("${internal.auth.secret}")
    private String internalAuthSecret;

    @Value("${internal.auth.header-name:X-Internal-Auth}")
    private String internalAuthHeaderName;

    /**
     * 配置 Feign 请求拦截器
     * 自动为所有 Feign 请求添加内部服务认证头
     */
    @Bean
    public RequestInterceptor internalAuthRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header(internalAuthHeaderName, internalAuthSecret);
                log.debug("Feign 请求添加内部认证头: {} = {}", internalAuthHeaderName, internalAuthSecret);
            }
        };
    }
}

