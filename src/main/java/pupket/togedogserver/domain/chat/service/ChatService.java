package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, ChattingResponseDto> redisTemplateForSave;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final RedisTemplate<String,ChannelTopic> redisTopicTemplate;

    public ChatRoom getOrCreateChatRoom(Long sender, Long receiver) {
        return chatRoomRepository.findBySenderAndReceiver(sender, receiver)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .receiver(receiver)
                            .sender(sender)
                            .lastTime(Timestamp.valueOf(LocalDateTime.now()))
                            .build();
                    chatRoomRepository.save(newChatRoom);
                    ChannelTopic topic = new ChannelTopic("/sub/chat/room/" + newChatRoom.getRoomId());
                    redisTopicTemplate.opsForValue().set("chatTopic:" + newChatRoom.getRoomId(), topic);
                    return newChatRoom;
                });
    }

    public String calculateTimeAgo(Timestamp lastTime) {
        long diffInMillis = System.currentTimeMillis() - lastTime.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

        if (diffInMinutes < 60) {
            return diffInMinutes + "분 전";
        } else {
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            if (diffInHours < 24) {
                return diffInHours + "시간 전";
            } else {
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                return diffInDays + "일 전";
            }
        }
    }

    public List<ChatRoomResponseDto> getChatRoomList(Long uuid) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySender(uuid);
        List<ChatRoomResponseDto> chatRoomList = new ArrayList<>();
        for (ChatRoom room : chatRooms) {
            ChatRoomResponseDto chatroom = new ChatRoomResponseDto();
            chatroom.setRoomId(room.getRoomId());
            chatroom.setLastTime(room.getLastTime());
            chatroom.setSender(userRepository.findById(room.getSender())
                    .orElseThrow(() -> new MateException(ExceptionCode.NOT_FOUND_MATE))
                    .getNickname());
            chatRoomList.add(chatroom);
        }
        return chatRoomList;
    }

    public void saveChatToRedis(String roomId, ChattingResponseDto chat) {
        String key = "chatRoomId:" + roomId;

        // Redis에 메시지 저장
        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1); // opsForList 사용
        if (chatList == null) {
            chatList = new ArrayList<>();
        }

        boolean isDuplicate = chatList.stream().anyMatch(savedChat ->
                savedChat.getLastTime().equals(chat.getLastTime()) &&
                        savedChat.getContent().equals(chat.getContent())
        );

        if (!isDuplicate) {
            redisTemplateForSave.opsForList().rightPush(key, chat);
            redisTemplateForSave.expire(key, 3, TimeUnit.DAYS);
        } else {
            log.warn("Duplicate message detected. Not saving to Redis.");
        }
    }

    public void leaveRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }

    public Timestamp getParsedLastTime(String lastTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }

    // 마지막으로 받은 시간 이후의 메시지들을 조회하는 메서드
    public List<ChattingResponseDto> getMessagesAfterLastTime(Long roomId, Timestamp lastTime) {
        String key = "chatRoomId:" + roomId;

        // Redis에서 해당 채팅방의 전체 메시지 조회 (opsForList로 ChattingResponseDto 리스트 가져오기)
        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1);

        if (chatList == null || chatList.isEmpty()) {
            log.warn("No messages found for roomId: {}", roomId);
            return new ArrayList<>();  // 데이터가 없을 경우 빈 리스트 반환
        }

        // 마지막으로 받은 시간 이후의 메시지 필터링
        List<ChattingResponseDto> unreceivedMessages = new ArrayList<>();
        for (ChattingResponseDto message : chatList) {
            if (message.getLastTime().after(lastTime)) {
                log.info("수행되었음");
                unreceivedMessages.add(message);
            }
            log.info("수행안됨");
            log.info(message.getLastTime()+"");
            log.info("result={}",message.getLastTime().after(lastTime));
        }

        return unreceivedMessages;
    }
}