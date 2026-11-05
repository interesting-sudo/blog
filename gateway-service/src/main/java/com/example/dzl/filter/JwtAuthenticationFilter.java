package com.example.dzl.filter;

import com.example.dzl.JwtUtil;
import com.example.dzl.config.GatewayConfig;
import com.example.dzl.enums.CommonErrorEnum;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    @Autowired
    private GatewayConfig gatewayConfig;  // 注入配置类
    @Autowired
    private JwtUtil jwtUtil;

    // 约定：管理员角色 ID 为 1
    private static final Long ADMIN_ROLE_ID = 1L;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String uri = request.getURI().toString();
        long startTime = System.currentTimeMillis();
        log.info("请求【{}】开始，时间：{}", uri,startTime);

        // 判断请求路径是否在放行列表中
        if (isExcludedPath(request.getURI().getPath())) {
            // 如果是放行的路径，直接通过
            return chain.filter(exchange);
        }

        // 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("请求【{}】缺少有效的 Authorization 请求头", uri);
            return buildErrorResponse(response, CommonErrorEnum.UNAUTHORIZED);
        }

        // 提取 token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(BEARER_PREFIX.length());

        Claims claims;
        // 验证 token
        try {
           claims = jwtUtil.verifyAccessToken(token);
        } catch (Exception e) {
            log.debug("请求【{}】的 token 验证失败，错误信息：{}", uri, e.getMessage());
            return buildErrorResponse(response, CommonErrorEnum.UNAUTHORIZED);
        }
        // 角色鉴权：如果是访问 /api/admin/ 开头的接口，必须是管理员
        if(request.getURI().getPath().startsWith("/api/admin")){
            Long roleId = jwtUtil.getRoleId(claims);
            if (!ADMIN_ROLE_ID.equals(roleId)) {
                log.warn("非法越权访问！用户 ID: {}, 尝试访问管理员接口: {}", jwtUtil.getUserId(claims), request.getURI().getPath());
                return buildErrorResponse(response, CommonErrorEnum.FORBIDDEN);
            }
        }

        Mono<Void> filter = chain.filter(exchange)
                .doFinally(result -> {
                    // ============以下是后置逻辑=============
                    long endTime = System.currentTimeMillis();
                    log.info("请求【{}】结束：时间：{}，耗时：{}ms", uri, endTime, endTime - startTime);
                });
        return filter;


    }

    @Override
    public int getOrder() {
        return -1; // 优先级最高
    }

    // 判断请求路径是否需要放行
    private boolean isExcludedPath(String path) {
        for (String excludedPath : gatewayConfig.getExcludedPaths()) {
            if (path.matches(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建错误响应（统一返回 Result 格式）
     *
     * @param response HTTP 响应对象
     * @param errorEnum 错误枚举
     * @return Mono<Void>
     */
    private Mono<Void> buildErrorResponse(ServerHttpResponse response, CommonErrorEnum errorEnum) {
        // 设置响应状态码和内容类型
        response.setStatusCode(errorEnum.getHttpStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 手动拼接 JSON 字符串（简单直接）
        String json = String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}",
                errorEnum.getCode(), errorEnum.getMsg());

        // 将 JSON 字符串转为字节数组，然后写入响应
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}