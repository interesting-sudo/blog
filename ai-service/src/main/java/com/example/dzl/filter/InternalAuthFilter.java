package com.example.dzl.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 内部服务认证过滤器
 * 用于验证请求是否来自 gateway-service，防止直接访问内部服务接口
 */
@Component
@Slf4j
public class InternalAuthFilter implements Filter {

    @Value("${internal.auth.secret}")
    private String internalAuthSecret;

    @Value("${internal.auth.header-name:X-Internal-Auth}")
    private String internalAuthHeaderName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求路径
        String requestPath = httpRequest.getRequestURI();

        // 获取内部认证头
        String internalAuth = httpRequest.getHeader(internalAuthHeaderName);

        // 验证内部认证头是否存在且正确
        if (internalAuth == null || !internalAuth.equals(internalAuthSecret)) {
            log.warn("检测到直接访问内部服务接口：{}，IP：{}", requestPath, httpRequest.getRemoteAddr());

            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":40300,\"msg\":\"禁止直接访问内部服务接口，请通过网关访问！\",\"data\":null}");
            return;
        }

        // 验证通过，继续执行后续过滤器
        chain.doFilter(request, response);
    }
}
