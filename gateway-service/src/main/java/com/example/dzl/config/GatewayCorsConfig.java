package com.example.dzl.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Spring Cloud Gateway 跨域配置（基于 WebFlux，生效于所有经过网关的请求）
 */
@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 1. 构建跨域配置对象
        CorsConfiguration corsConfig = new CorsConfiguration();
        // 允许携带Cookie（前后端分离场景如果需要登录态，必须开启）
        corsConfig.setAllowCredentials(true);
        // 允许的请求来源（* 表示允许所有，生产环境建议指定具体前端域名，如 https://xxx.com）
        corsConfig.addAllowedOriginPattern("*");
        // 允许的请求方法（GET/POST/PUT/DELETE/OPTIONS 全覆盖）
        corsConfig.addAllowedMethod("*");
        // 允许的请求头（* 表示允许所有）
        corsConfig.addAllowedHeader("*");
        // 暴露的响应头（前端需要获取的自定义响应头，如 Token，不配置则前端无法获取）
//        corsConfig.addExposedHeader("Authorization");
        // 跨域预检请求的有效期（单位：秒，3600 表示 1 小时内无需重复发送预检请求）
        corsConfig.setMaxAge(3600L);

        // 2. 构建路径匹配器，配置跨域规则生效的路径（/** 表示所有路径）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", corsConfig);

        // 3. 构建并返回跨域过滤器
        return new CorsWebFilter(source);
    }
}