package pupket.togedogserver.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void sendMessage(String message) {
        try{
            ChattingRequestDto requestMessage = objectMapper.readValue(message, ChattingRequestDto.class);
            log.info("chattingRequestDto: {}", requestMessage.getContent());
            messagingTemplate.convertAndSend("/sub/chat/room/" + requestMessage.getRoomId(), requestMessage);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }


}
