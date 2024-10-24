package pupket.togedogserver.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void sendMessage(String message) {
        try{
            ChattingResponseDto requestMessage = objectMapper.readValue(message, ChattingResponseDto.class);
            log.info("chattingRequestDto: {}", requestMessage.getContent());
            messagingTemplate.convertAndSend("/sub/chat/room/" + requestMessage.getRoomId(), requestMessage);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }


}
