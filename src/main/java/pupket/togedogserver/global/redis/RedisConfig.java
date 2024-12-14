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
    private String port;

    //RedisConnectionFactory 빈을 생성하는 메서드
    //Redis서버와의 연결을 설정하고 관리하는데 사용

    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(Integer.parseInt(port));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    private <K, V> RedisTemplate<K, V> createRedisTemplate(RedisConnectionFactory factory,
                                                           RedisSerializer<K> keySerializer,
                                                           RedisSerializer<V> valueSerializer) {
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        return redisTemplate;
    }
    public RedisTemplate<String, String> customStringRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new StringRedisSerializer());
    }

    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(Object.class));
    }

    @Bean
    public RedisTemplate<String, ChattingResponseDto> chattingResponseRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChattingResponseDto.class));
    }

    @Bean
    public RedisTemplate<String, ChattingRequestDto> chattingRequestRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChattingRequestDto.class));
    }

    @Bean
    public RedisTemplate<String, ChannelTopic> channelTopicRedisTemplate(RedisConnectionFactory factory) {
        return createRedisTemplate(factory, new StringRedisSerializer(), new Jackson2JsonRedisSerializer<>(ChannelTopic.class));
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /**
     * redis 에 발행(publish)된 메시지 처리를 위한 리스너 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(
            MessageListenerAdapter listenerAdapterChatMessage,
            ChannelTopic channelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(listenerAdapterChatMessage, channelTopic);
        return container;
    }
}