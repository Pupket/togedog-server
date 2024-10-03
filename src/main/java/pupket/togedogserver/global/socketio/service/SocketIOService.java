package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingDto;
import pupket.togedogserver.domain.chat.dto.KafkaChattingDto;
import pupket.togedogserver.domain.notification.dto.NotificationRequest;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final FcmService fcmService;
    private final S3FileUtilImpl s3FileUtil;
    private final KafkaTemplate kafkaTemplate;

    private final List<KafkaChattingDto> undeliveredChats = new ArrayList<>();

    // Kafka에서 메시지를 수신하여 리스트에 추가
    @KafkaListener(topics = "undelivered-chats", groupId = "group_id")
    public void listen(KafkaChattingDto message) {
        synchronized (undeliveredChats) {
            undeliveredChats.add(message);
        }
    }

    public void sendChatting(SocketIOClient senderClient, ChattingDto chat) throws ExecutionException, InterruptedException {
        String room = senderClient.getHandshakeData().getSingleUrlParam("room");
        boolean messageDelivered = false;

        Long undeliveredRecipient = null;

        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                // 클라이언트가 연결되어 있으면 메시지 전송
                if (client.isChannelOpen()) {
                    client.sendEvent("read_message", chat);

                    NotificationRequest notification = new NotificationRequest();
                    notification.setReceiver(chat.getUserId());
                    notification.setTitle(String.valueOf(chat.getUserId()));
                    notification.setMessage(chat.getContent());
                    notification.setImage(s3FileUtil.upload(chat.getImage()));
                    fcmService.sendNotification(notification);

                    messageDelivered = true; // 메시지가 적어도 하나의 클라이언트에게 전달됨
                } else {
                    undeliveredRecipient = Long.valueOf(client.getSessionId().toString());
                }
            }
        }

        // 메시지가 전송되지 않았다면 Kafka에 저장
        if (!messageDelivered) {
            KafkaChattingDto kafkaChattingDto = new KafkaChattingDto();
            kafkaChattingDto.setSender(chat.getUserId());
            kafkaChattingDto.setReceiver(undeliveredRecipient);
            kafkaChattingDto.setContent(chat.getContent());
            kafkaChattingDto.setImage(chat.getImage());
            kafkaChattingDto.setLastTime(chat.getLastTime());
            kafkaTemplate.send("undelivered-chats", room, chat);
        }
    }

    public List<ChattingDto> fetchBacklogChats(String room) {
        return getBacklogChats(Long.valueOf(room));
    }

    private List<ChattingDto> getBacklogChats(Long room) {
        List<ChattingDto> chats = new ArrayList<>();

        for (KafkaChattingDto dto : undeliveredChats) {
            if (dto.getReceiver().equals(room)) {
                ChattingDto chatting = new ChattingDto();
                chatting.setLastTime(dto.getLastTime());
                chatting.setContent(dto.getContent());
                chatting.setImage(dto.getImage());
                chats.add(chatting);
            }
        }

        return chats;
    }

}
