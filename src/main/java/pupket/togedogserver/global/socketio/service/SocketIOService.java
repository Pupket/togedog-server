package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final FcmService fcmService;
    private final S3FileUtilImpl s3FileUtil;
    private final RedisTemplate<String, List<ChattingResponseDto>> redisTemplate;

    public void sendChatting(SocketIOClient senderClient, ChattingRequestDto chattingRequestDto) throws ExecutionException, InterruptedException {
        String room = senderClient.getHandshakeData().getSingleUrlParam("room");

        String imageUrl = s3FileUtil.upload(chattingRequestDto.getImage());
        ChattingResponseDto chattingResponseDto = new ChattingResponseDto();
        chattingResponseDto.setLastTime(chattingRequestDto.getLastTime());
        chattingResponseDto.setUserId(chattingRequestDto.getUserId());
        chattingResponseDto.setContent(chattingRequestDto.getContent());
        chattingResponseDto.setImage(imageUrl);

        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                if (client.isChannelOpen()) {
                    client.sendEvent("read_message", chattingResponseDto);
                } else {
                    sendChatToFcm(chattingResponseDto, room);
                }
            }
        }

        // todo:: redis에 채팅 저장
    }

    private void sendChatToFcm(ChattingResponseDto chat, String roomId) throws ExecutionException, InterruptedException {
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setReceiver(chat.getUserId());
        notification.setTitle(String.valueOf(chat.getUserId()));
        notification.setMessage(chat.getContent());
        notification.setImage(chat.getImage());
        fcmService.sendNotification(notification, roomId);
    }

    private void sendChatToRedis(String room, List<ChattingResponseDto> chats) {
        redisTemplate.opsForValue().set("room: " + room, chats, 3, TimeUnit.DAYS);
    }

}
