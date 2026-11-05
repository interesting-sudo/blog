package com.example.dzl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // 配置密码加密功能
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 禁用默认的安全配置，允许所有请求访问
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();  // 禁用 CSRF 防护
        http.authorizeRequests()
                .anyRequest().permitAll();  // 所有请求不需要认证，直接放行
        // 禁用默认的表单登录页面
        http.formLogin().disable();
        http.httpBasic().disable();  // 禁用 HTTP 基本认证
    }
}
