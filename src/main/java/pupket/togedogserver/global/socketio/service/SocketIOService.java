package pupket.togedogserver.global.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.dto.KafkaChattingDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final ChatRoomRepository chatRoomRepository;
    private final FcmService fcmService;
    private final S3FileUtilImpl s3FileUtil;
    private final KafkaTemplate kafkaTemplate;

    Long kafkaKeyForUserId;
    private final List<KafkaChattingDto> undeliveredChats = new ArrayList<>();

    // Kafka에서 메시지를 수신하여 리스트에 추가
    @KafkaListener(topics = "undelivered-chats", groupId = "chatting")
    public void listen(ConsumerRecord<Long, KafkaChattingDto> chatting) {
        if (kafkaKeyForUserId.equals(chatting.key())) {
            synchronized (undeliveredChats) {
                undeliveredChats.add(chatting.value());
            }
        }
    }

    public void sendChatting(SocketIOClient senderClient, ChattingRequestDto chattingRequestDto) throws ExecutionException, InterruptedException {
        String room = senderClient.getHandshakeData().getSingleUrlParam("room");
        boolean messageDelivered = false;

        String imageUrl = s3FileUtil.upload(chattingRequestDto.getImage());
        ChattingResponseDto chattingResponseDto = new ChattingResponseDto();
        chattingResponseDto.setLastTime(chattingRequestDto.getLastTime());
        chattingResponseDto.setUserId(chattingRequestDto.getUserId());
        chattingResponseDto.setContent(chattingRequestDto.getContent());
        chattingResponseDto.setImage(imageUrl);

        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                // 클라이언트가 연결되어 있으면 메시지 전송
                if (client.isChannelOpen()) {
                    client.sendEvent("read_message", chattingResponseDto);
                    messageDelivered = true; // 클라이언트가 채팅을 수신함
                    sendChatToFcm(chattingResponseDto, room);
                }
            }
        }

        // 채팅 미전송시 kafka 서버에 임시 저장
        if (!messageDelivered) {
            sendChatToKafka(chattingResponseDto);
        }

        saveRecentChat(chattingResponseDto, room);
    }

    private void sendChatToKafka(ChattingResponseDto chat) {
        KafkaChattingDto kafkaChattingDto = new KafkaChattingDto();
        kafkaChattingDto.setUserId(chat.getUserId());
        kafkaChattingDto.setContent(chat.getContent());
        kafkaChattingDto.setImage(chat.getImage());
        kafkaChattingDto.setLastTime(chat.getLastTime());
        kafkaTemplate.send("undelivered-chats", chat.getUserId(), chat);
    }

    private void sendChatToFcm(ChattingResponseDto chat, String roomId) throws ExecutionException, InterruptedException {
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setReceiver(chat.getUserId());
        notification.setTitle(String.valueOf(chat.getUserId()));
        notification.setMessage(chat.getContent());
        notification.setImage(chat.getImage());
        fcmService.sendNotification(notification, roomId);
    }

    private void saveRecentChat(ChattingResponseDto chat, String room) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(Long.valueOf(room)).get().builder()
                .roomId(Long.valueOf(room))
                .lastTime(chat.getLastTime())
                .user1(chat.getUserId())
                .content(chat.getContent())
                .content(chat.getImage())
                .build();
        chatRoomRepository.save(chatRoom);
    }

    public List<ChattingResponseDto> fetchBacklogChats(String room) {
        return getBacklogChats(Long.valueOf(room));
    }

    private List<ChattingResponseDto> getBacklogChats(Long room) {
        List<ChattingResponseDto> chats = new ArrayList<>();

        for (KafkaChattingDto dto : undeliveredChats) {
            ChattingResponseDto chatting = new ChattingResponseDto();
            chatting.setLastTime(dto.getLastTime());
            chatting.setUserId(dto.getUserId());
            chatting.setContent(dto.getContent());
            chatting.setImage(dto.getImage());
            chats.add(chatting);
        }

        return chats;
    }

}
