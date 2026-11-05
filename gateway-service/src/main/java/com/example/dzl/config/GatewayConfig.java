package com.example.dzl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class GatewayConfig {
    private String[] excludedPaths;

    @Value("${internal.auth.secret}")
    private String internalAuthSecret;

    @Value("${internal.auth.header-name}")
    private String internalAuthHeaderName;

    /**
     * 自定义路由配置
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.
                        path("/api/user/**")
                        .filters(f -> f
                                .rewritePath("/api(/user.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                                 )

                        .uri("lb://user-service")

                )
                .route(r -> r.
                        path("/api/admin/user/**")
                        .filters(f -> f
                                .rewritePath("/api/admin(/user.*)","/admin$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)
                        )
                        .uri("lb://user-service")
                )
                .route(r -> r.
                        path("/api/blog/**")
                        .filters(f -> f
                                .rewritePath("/api(/blog.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://blog-service"))
                .route(r -> r.
                        path("/api/admin/category/**")
                        .filters(f -> f
                                .rewritePath("/api/admin(/category.*)","/admin$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)
                        )
                        .uri("lb://blog-service")
                )
                .route(r -> r.
                        path("/api/admin/tag/**")
                        .filters(f -> f
                                .rewritePath("/api/admin(/tag.*)","/admin$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)
                        )
                        .uri("lb://blog-service")
                )
                .route(r -> r.
                        path("/api/admin/blog/**")
                        .filters(f -> f
                                .rewritePath("/api/admin(/blog.*)","/admin$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)
                        )
                        .uri("lb://blog-service")
                )
                .route(r -> r.
                        path("/api/document/**")
                        .filters(f -> f
                                .rewritePath("/api(/document.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://document-service"))

                .route(r -> r.
                        path("/api/comment/**")
                        .filters(f -> f
                                .rewritePath("/api(/comment.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://comment-service"))

                .route(r -> r.
                        path("/api/admin/comment/**")
                        .filters(f -> f
                                .rewritePath("/api/admin(/comment.*)","/admin/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://comment-service"))
                .route(r -> r.
                        path("/api/ai/**")
                        .filters(f -> f
                                .rewritePath("/api(/ai.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://ai-service"))

                .route(r -> r.
                        path("/api/search/**")
                        .filters(f -> f
                                .rewritePath("/api(/search.*)","/$1")
                                .addRequestHeader(internalAuthHeaderName, internalAuthSecret)  // 添加内部认证头
                        )
                        .uri("lb://search-service"))
                .build();
    }

}

