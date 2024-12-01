package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import pupket.togedogserver.domain.chat.controller.port.ChatService;
import pupket.togedogserver.domain.chat.dto.ChatRoomCreateResponse;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.service.port.ChatRoomRepository;
import pupket.togedogserver.domain.notification.controller.port.FcmService;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.ChatException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.s3.util.S3FileUtil;
import pupket.togedogserver.global.websocket.WebSocketEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, ChattingResponseDto> redisTemplateForSave;
    private final RedisTemplate<String, String> redisTemplateForUserStatus;
    private final UserRepository userRepository;
    private final FcmService fcmServiceImpl;
    private final RedisTemplate<String, ChannelTopic> redisTopicTemplate;
    private final S3FileUtil s3FileUtilImpl;
    private final RedisPublisher redisPublisher;
    private final WebSocketEventListener webSocketEventListener;


    @Override
    public ChatRoomCreateResponse getOrCreateChatRoom(Long sender, Long receiver, String roomTitle) {
        User findSender = findSender(sender);
        User findReceiver = findReceiver(receiver);

        String findSenderProfileImage = getProfileImage(findSender);
        String findReceiverProfileImage = getProfileImage(findReceiver);

        ChatRoom findChatRoom = createChatRoom(sender, receiver, roomTitle, findSenderProfileImage, findReceiverProfileImage);

        findChatRoom = validateChatRoom(roomTitle, findChatRoom);

        //findChatRoom의 sender(메이트)의 uuid와 findSender(현재 로그인 중인 유저)의 uuid가 일치하는 경우 현재 로그인 유저는 mate이기 때문에 채팅방 제목 반환
        // 일치하지 않는 경우에는 보호자가 로그인한 것이기 때문에 상대방 닉네임을 담아서 반환
        if (findChatRoom.getSender().equals(findSender.getUuid())) {
            return ChatRoomCreateResponse.builder()
                    .roomTitle(findChatRoom.getTitle())
                    .roomId(findChatRoom.getRoomId())
                    .build();
        }

        return ChatRoomCreateResponse.builder()
                .roomTitle(findReceiver.getNickname())
                .roomId(findChatRoom.getRoomId())
                .build();
    }

    private ChatRoom createChatRoom(Long sender, Long receiver, String roomTitle, String findSenderProfileImage, String findReceiverProfileImage) {
        ChatRoom findChatRoom = chatRoomRepository.findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(sender, receiver, roomTitle, receiver, sender, roomTitle)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.to(receiver, sender, findSenderProfileImage, roomTitle, findReceiverProfileImage);

                    chatRoomRepository.save(newChatRoom);

                    setTopicInRedisTemplate(newChatRoom);
                    return newChatRoom;
                });
        return findChatRoom;
    }

    private void setTopicInRedisTemplate(ChatRoom newChatRoom) {
        ChannelTopic topic = new ChannelTopic("/sub/chat/room/" + newChatRoom.getRoomId());
        redisTopicTemplate.opsForValue().set("chatTopic:" + newChatRoom.getRoomId(), topic);
    }

    private ChatRoom validateChatRoom(String roomTitle, ChatRoom findChatRoom) {
        if (findChatRoom.getTitle().isEmpty() && roomTitle != null) {
            ChatRoom updateChatRoom = findChatRoom.toBuilder()
                    .title(roomTitle)
                    .build();

            findChatRoom = chatRoomRepository.save(updateChatRoom);
        }
        return findChatRoom;
    }

    private String getProfileImage(User findSender) {
        return findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage();
    }

    private User findReceiver(Long receiver) {
        return userRepository.findById(receiver).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    private User findSender(Long sender) {
        return userRepository.findById(sender).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    @Override
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

    @Override
    public List<ChatRoomResponseDto> getChatRoomList(Long uuid) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(uuid, uuid);
        List<ChatRoomResponseDto> chatRoomList = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            User findSender = findSender(room.getSender());
            User findReceiver = findReceiver(room.getReceiver());

            Timestamp lastTime = room.getLastTime();
            List<ChattingResponseDto> unreceivedMessages = getMessagesAfterLastTime(room.getRoomId(), lastTime);

            ChatRoomResponseDto chatroom = ChatRoomResponseDto.to(room, findSender, findReceiver, unreceivedMessages);

            chatRoomList.add(chatroom);
        }
        return chatRoomList;
    }

    public void saveChatToRedis(String roomId, ChattingResponseDto chat) {
        String key = "chatRoomId:" + roomId;

        // Redis에 메시지 저장
        List<ChattingResponseDto> chatList = saveMessageInRedis(key);

        isDuplicate(chat, chatList, key);
    }

    private void isDuplicate(ChattingResponseDto chat, List<ChattingResponseDto> chatList, String key) {
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

    private List<ChattingResponseDto> saveMessageInRedis(String key) {
        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1); // opsForList 사용
        if (chatList == null) {
            chatList = new ArrayList<>();
        }
        return chatList;
    }

    @Override
    public void leaveRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }

    @Override
    public Timestamp getParsedLastTime(String lastTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }

    @Override
    public List<ChattingResponseDto> getMessagesAfterLastTime(Long roomId, Timestamp lastTime) {
        String key = "chatRoomId:" + roomId;

        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1);

        if (chatList == null || chatList.isEmpty()) {
            log.warn("No messages found for roomId: {}", roomId);
            return new ArrayList<>();  // 데이터가 없을 경우 빈 리스트 반환
        }

        // 마지막으로 받은 시간 이후의 메시지 필터링 및 정렬
        List<ChattingResponseDto> unreceivedMessages = chatList.stream()
                .filter(message -> message.getLastTime().after(lastTime))
                .sorted(Comparator.comparing(ChattingResponseDto::getLastTime).reversed())
                .collect(Collectors.toList());

        return unreceivedMessages;
    }


    @Override
    public void sendMessageToPublisher(ChattingRequestDto message) {
        Timestamp parsedLastTime = getParsedLastTime(message.getLastTime());
        ChatRoom findChatRoom = findChatRoom(message);

        Long receiver = findChatRoom.getReceiver().equals(message.getUserId()) ? findChatRoom.getSender() : findChatRoom.getReceiver();
        log.info("receiverId= {}", receiver);

        // 사용자 ID를 기반으로 세션 ID 가져오기
        String currentSessionId = webSocketEventListener.getCurrentSessionId();
        log.info("currentSessionId= {}", currentSessionId);
        if (currentSessionId == null || !webSocketEventListener.isSessionConnected(currentSessionId)) {
            log.warn("Current user is offline. Sending notification.");
            sendNotificationToDisConnectedUser(message, currentSessionId, findChatRoom, parsedLastTime, receiver);
        }

        ChattingResponseDto responseDto = ChattingResponseDto.to(message, parsedLastTime);

        // Redis에 메시지 저장
        saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

        // 메시지 발행
        redisPublisher.publish(responseDto);
    }

    private void sendNotificationToDisConnectedUser(ChattingRequestDto message, String sessionId, ChatRoom findChatRoom, Timestamp parsedLastTime, Long receiver) {
        // 세션 ID를 기반으로 연결 상태 확인
        NotificationRequestDto notificationRequestDto = NotificationRequestDto.to(message, findChatRoom, parsedLastTime,receiver);
        try {
            fcmServiceImpl.sendNotification(notificationRequestDto, receiver);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw new ChatException(ExceptionCode.INTERRUPTION_OR_EXECUTION_ERR);
        }
    }

    private ChatRoom findChatRoom(ChattingRequestDto message) {
        return chatRoomRepository.findById(message.getRoomId()).orElseThrow(
                () -> new ChatException(ExceptionCode.NOT_FOUND_CHATROOM)
        );
    }
}