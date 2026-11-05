package com.example.dzl.mq;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.dzl.feign.SearchServiceClient;
import com.example.dzl.mapper.BlogMapper;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.mq.UserProfileChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "${mq.topic.user-profile-changed}",
        consumerGroup = "blog-service-user-profile-consumer"
)
public class UserProfileChangedConsumerForBlog implements RocketMQListener<UserProfileChangedEvent> {

    private final BlogMapper blogMapper;

    private final SearchServiceClient searchServiceClient;

    public UserProfileChangedConsumerForBlog(BlogMapper blogMapper,
                                             SearchServiceClient searchServiceClient) {
        this.blogMapper = blogMapper;
        this.searchServiceClient = searchServiceClient;
    }

    @Override
    public void onMessage(UserProfileChangedEvent event) {
        log.info("blog-service 收到用户资料变更消息: {}", event);

        LambdaUpdateWrapper<Blog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Blog::getAuthorId, event.getUserId())
                .set(event.getNickname() != null, Blog::getAuthorName, event.getNickname())
                .set(event.getAvatar() != null, Blog::getAuthorAvatar, event.getAvatar());

        int rows = blogMapper.update(null, updateWrapper);
        log.info("blog-service 更新博客作者冗余字段条数: {}", rows);

        updateAuthorProfileInEsWithRetry(event);
    }

    /**
     * 重试更新ES作者信息
     * @param event 用户个人信息变更事件
     */
    private void updateAuthorProfileInEsWithRetry(UserProfileChangedEvent event) {
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                searchServiceClient.updateAuthorProfile(event);
                return;
            } catch (Exception e) {
                try {
                    log.info("重试第{}次, 更新ES作者信息失败，userId={}", attempts, event.getUserId(), e);
                    Thread.sleep(200L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}

