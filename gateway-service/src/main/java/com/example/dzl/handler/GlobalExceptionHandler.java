package com.example.dzl.handler;

import com.example.dzl.enums.CommonErrorEnum;
import com.example.dzl.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关层全局异常处理，覆盖默认的HTML/JSON错误响应
 */
@Configuration
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // 只处理 JSON
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<?> result;

        if (ex instanceof NotFoundException) {
            // 路由不存在/未匹配 -> 404
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            result = Result.error(CommonErrorEnum.NOT_FOUND.getCode(), CommonErrorEnum.NOT_FOUND.getMsg());
        } else if (ex instanceof ResponseStatusException rse) {
            // 其他带状态码的异常
            exchange.getResponse().setStatusCode(rse.getStatus());
            // 如果状态码是404，也使用 CommonErrorEnum.NOT_FOUND
            if (rse.getStatus() == HttpStatus.NOT_FOUND) {
                result = Result.error(CommonErrorEnum.NOT_FOUND.getCode(), CommonErrorEnum.NOT_FOUND.getMsg());
            } else {
                result = Result.error(CommonErrorEnum.INTERNAL_ERROR.getCode(), rse.getReason() == null ? "网关异常" : rse.getReason());
            }
        } else {
            // 兜底
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            result = Result.error(CommonErrorEnum.INTERNAL_ERROR.getCode(), CommonErrorEnum.INTERNAL_ERROR.getMsg());
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (Exception e) {
            bytes = ("{\"code\":" + CommonErrorEnum.INTERNAL_ERROR.getCode() + ",\"msg\":\"网关序列化失败\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
