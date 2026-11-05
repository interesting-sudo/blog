package com.example.dzl;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonWebAutoConfiguration {
    /**
     * 关键：允许业务服务“自定义覆盖”
     * - 如果某个服务自己定义了 GlobalExceptionHandler Bean
     * - starter 这里就不再注入，避免冲突
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
