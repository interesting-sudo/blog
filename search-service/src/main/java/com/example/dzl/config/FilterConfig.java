package com.example.dzl.config;


import com.example.dzl.filter.InternalAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * 用于注册内部认证过滤器
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilterRegistration(InternalAuthFilter filter) {
        FilterRegistrationBean<InternalAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");  // 拦截所有请求
        registration.setName("internalAuthFilter");
        registration.setOrder(1);  // 设置优先级，数字越小优先级越高
        return registration;
    }
}
