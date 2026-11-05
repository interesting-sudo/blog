package com.example.dzl.mq;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.dzl.mapper.CommentMapper;
import com.example.dzl.model.entity.Comment;
import com.example.dzl.model.mq.UserProfileChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "${mq.topic.user-profile-changed}",
        consumerGroup = "comment-service-user-profile-consumer"
)
public class UserProfileChangedConsumerForComment implements RocketMQListener<UserProfileChangedEvent> {

    private final CommentMapper commentMapper;

    public UserProfileChangedConsumerForComment(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public void onMessage(UserProfileChangedEvent event) {
        log.info("comment-service 收到用户资料变更消息: {}", event);

        LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Comment::getUserId, event.getUserId())
                .set(event.getNickname() != null, Comment::getNickname, event.getNickname())
                .set(event.getAvatar() != null, Comment::getUserAvatar, event.getAvatar());

        int rows = commentMapper.update(null, updateWrapper);
        log.info("comment-service 更新评论用户冗余字段条数: {}", rows);
    }
}


