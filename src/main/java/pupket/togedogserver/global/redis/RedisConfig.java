package pupket.togedogserver.global.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.service.RedisSubscriber;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 서버 연결 설정
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis Topic 설정
     */
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    /**
     * RedisTemplate 생성 메서드
     */
    private <K, V> RedisTemplate<K, V> createRedisTemplate(
            RedisConnectionFactory factory,
            RedisSerializer<K> keySerializer,
            RedisSerializer<V> valueSerializer) {
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * RedisTemplate - String, String
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new StringRedisSerializer());
    }

    /**
     * RedisTemplate - String, Object
     */
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(Object.class));
    }

    /**
     * RedisTemplate - String, ChattingResponseDto
     */
    @Bean
    public RedisTemplate<String, ChattingResponseDto> chattingResponseRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChattingResponseDto.class));
    }

    /**
     * RedisTemplate - String, ChattingRequestDto
     */
    @Bean
    public RedisTemplate<String, ChattingRequestDto> chattingRequestRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChattingRequestDto.class));
    }

    /**
     * RedisTemplate - String, ChannelTopic
     */
    @Bean
    public RedisTemplate<String, ChannelTopic> channelTopicRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChannelTopic.class));
    }

    /**
     * Redis Message Listener 설정
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /**
     * Redis Message Listener Container 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }
}