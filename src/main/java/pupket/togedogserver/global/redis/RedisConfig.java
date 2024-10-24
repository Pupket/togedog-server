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
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.service.RedisSubscriber;

import java.util.List;

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

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //키를 위한 직렬화 설정
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Key를 Long 타입으로 처리
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //값을 위한 직렬화 설정
        Jackson2JsonRedisSerializer<ChatRoom> serializer = new Jackson2JsonRedisSerializer<>(ChatRoom.class);
        redisTemplate.setValueSerializer(serializer);  // Value를 JSON 형태로 직렬화
        redisTemplate.setHashValueSerializer(serializer);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ChattingResponseDto> redisTemplateForSave() {
        RedisTemplate<String,ChattingResponseDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //키를 위한 직렬화 설정
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Key를 Long 타입으로 처리
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //값을 위한 직렬화 설정
        Jackson2JsonRedisSerializer<ChattingResponseDto> serializer = new Jackson2JsonRedisSerializer<>(ChattingResponseDto.class);
        redisTemplate.setValueSerializer(serializer);  // Value를 JSON 형태로 직렬화
        redisTemplate.setHashValueSerializer(serializer);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ChattingResponseDto> redisTemplateForResponse() {
        RedisTemplate<String,ChattingResponseDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //키를 위한 직렬화 설정
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Key를 Long 타입으로 처리
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //값을 위한 직렬화 설정
        Jackson2JsonRedisSerializer<ChattingResponseDto> serializer = new Jackson2JsonRedisSerializer<>(ChattingResponseDto.class);
        redisTemplate.setValueSerializer(serializer);  // Value를 JSON 형태로 직렬화
        redisTemplate.setHashValueSerializer(serializer);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ChannelTopic> redisTopicTemplate() {
        RedisTemplate<String, ChannelTopic> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(ChannelTopic.class)); // ChannelTopic 직렬화
        return redisTemplate;
    }


    // ChattingResponseDto용 RedisTemplate
    @Bean
    public RedisTemplate<String, List<ChattingResponseDto>> redisChattingTemplate() {
        RedisTemplate<String, List<ChattingResponseDto>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //키를 위한 직렬화 설정
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Key를 Long 타입으로 처리
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //값을 위한 직렬화 설정
        Jackson2JsonRedisSerializer<ChattingResponseDto> serializer = new Jackson2JsonRedisSerializer<>(ChattingResponseDto.class);
        redisTemplate.setValueSerializer(serializer);  // Value를 JSON 형태로 직렬화
        redisTemplate.setHashValueSerializer(serializer);
        return redisTemplate;
    }

    // ChattingRequestDto용 RedisTemplate
    @Bean
    public RedisTemplate<String, List<ChattingRequestDto>> redisChattingRequestTemplate() {
        RedisTemplate<String, List<ChattingRequestDto>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //키를 위한 직렬화 설정
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Key를 Long 타입으로 처리
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //값을 위한 직렬화 설정
        Jackson2JsonRedisSerializer<ChattingRequestDto> serializer = new Jackson2JsonRedisSerializer<>(ChattingRequestDto.class);
        redisTemplate.setValueSerializer(serializer);  // Value를 JSON 형태로 직렬화
        redisTemplate.setHashValueSerializer(serializer);
        return redisTemplate;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /**
     * redis 에 발행(publish)된 메시지 처리를 위한 리스너 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener (
            MessageListenerAdapter listenerAdapterChatMessage,
            ChannelTopic channelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(listenerAdapterChatMessage, channelTopic);
        return container;
    }
}