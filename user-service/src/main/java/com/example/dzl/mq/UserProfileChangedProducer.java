package com.example.dzl.mq;


import com.example.dzl.model.mq.UserProfileChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProfileChangedProducer {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${mq.topic.user-profile-changed}")
    private String topic;

    public UserProfileChangedProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void send(UserProfileChangedEvent event) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("发送用户资料变更消息, attempt={}: {}", attempt, event);
                rocketMQTemplate.convertAndSend(topic, event);
                return;
            } catch (Exception e) {
                log.error("发送用户资料变更消息第{}次失败: {}", attempt, event, e);
                if (attempt == maxAttempts) {
                    log.error("发送用户资料变更消息重试次数耗尽: {}", event);
                }
            }
        }
    }
}

