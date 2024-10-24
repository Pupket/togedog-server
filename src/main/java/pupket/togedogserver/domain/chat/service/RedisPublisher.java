package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisPublisher {
    private final ChannelTopic topic;
    private final RedisTemplate redisTemplate;


    public void publish(ChattingResponseDto message){

        redisTemplate.convertAndSend(topic.getTopic(),message);
    }
}
