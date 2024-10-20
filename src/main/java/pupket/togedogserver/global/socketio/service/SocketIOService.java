package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.ChatException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final FcmService fcmService;
    private final RedisTemplate<String, List<ChattingResponseDto>> redisTemplate;

    @Transactional
    public void sendChatting(SocketIOClient senderClient, ChattingRequestDto chattingRequestDto) throws ExecutionException, InterruptedException {
        String room = senderClient.getHandshakeData().getSingleUrlParam("room");
        String chattingRequestDtoImage = chattingRequestDto.getImage();

        ChattingResponseDto chattingResponseDto =  ChattingResponseDto.builder()
                .lastTime(getParsedLastTime(chattingRequestDto.getLastTime()))
                .userId(chattingRequestDto.getUserId())
                .content(chattingRequestDto.getContent())
                .image(chattingRequestDtoImage)
                .build();

        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                if (client.isChannelOpen()) {
                    client.sendEvent("read_message", chattingResponseDto);
                } else {
                    sendChatToFcm(chattingResponseDto, room);
                }
            }
        }
        // Redis에 채팅 저장
        saveChatToRedis(room, chattingResponseDto);

    }
    private Timestamp getParsedLastTime(String lastTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        }catch (Exception e){
            return new Timestamp(System.currentTimeMillis());
        }
    }

    private void sendChatToFcm(ChattingResponseDto chat, String roomId) throws ExecutionException, InterruptedException {
        NotificationRequestDto notification = NotificationRequestDto.builder()
                .receiver(chat.getUserId())
                .title(String.valueOf(chat.getUserId()))
                .message(chat.getContent())
                .build();

        fcmService.sendNotification(notification, roomId);
    }

    private void saveChatToRedis(String room, ChattingResponseDto chat) {
        String key = "chatRoomId:" + room;
        List<ChattingResponseDto> chatList = redisTemplate.opsForValue().get(key);
        if (chatList == null) {
            chatList = new ArrayList<>();
        }
        chatList.add(chat);
        try{
            redisTemplate.opsForValue().set(key, chatList, 3, TimeUnit.DAYS); // 3일간 저장
        }catch (Exception e){
            throw new ChatException(ExceptionCode.REDIS_CONNECTION_FAILURE);
        }
    }

}
