package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
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
import pupket.togedogserver.global.websocket.WebSocketEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, ChattingResponseDto> redisTemplateForSave;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final FcmService fcmServiceImpl;
    private final RedisTemplate<String, ChannelTopic> redisTopicTemplate;
    private final RedisPublisher redisPublisher;
    private final WebSocketEventListener webSocketEventListener;

    @Override
    public ChatRoomCreateResponse getOrCreateChatRoom(Long sender, Long receiver, String roomTitle) {
        log.info("Getting or creating chat room. Sender: {}, Receiver: {}, RoomTitle: {}", sender, receiver, roomTitle);

        User findSender = findSender(sender);
        User findReceiver = findReceiver(receiver);

        log.info("Sender found: {}. Receiver found: {}", findSender, findReceiver);

        String findSenderProfileImage = getProfileImage(findSender);
        String findReceiverProfileImage = getProfileImage(findReceiver);

        ChatRoom findChatRoom = createChatRoom(sender, receiver, roomTitle, findSenderProfileImage, findReceiverProfileImage);

        findChatRoom = validateChatRoom(roomTitle, findChatRoom);

        log.info("Chat room created or validated: {}", findChatRoom);

        if (findChatRoom.getOwner().equals(findSender.getUuid())) {
            log.info("Returning chat room with title: {}", findChatRoom.getTitle());
            return ChatRoomCreateResponse.builder()
                    .roomTitle(findChatRoom.getTitle())
                    .roomId(findChatRoom.getRoomId())
                    .build();
        }

        log.info("Returning chat room with receiver's nickname: {}", findReceiver.getNickname());
        return ChatRoomCreateResponse.builder()
                .roomTitle(findReceiver.getNickname())
                .roomId(findChatRoom.getRoomId())
                .build();
    }

    private ChatRoom createChatRoom(Long sender, Long receiver, String roomTitle, String findSenderProfileImage, String findReceiverProfileImage) {
        log.info("Creating chat room if not exists. Sender: {}, Receiver: {}, Title: {}", sender, receiver, roomTitle);
        return chatRoomRepository.findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(sender, receiver, roomTitle, receiver, sender, roomTitle)
                .orElseGet(() -> {
                    log.info("No existing chat room found. Creating a new one.");
                    ChatRoom newChatRoom = ChatRoom.to(receiver, sender, findSenderProfileImage, roomTitle, findReceiverProfileImage);

                    chatRoomRepository.save(newChatRoom);
                    log.info("New chat room saved: {}", newChatRoom);

                    setTopicInRedisTemplate(newChatRoom);
                    return newChatRoom;
                });
    }

    private void setTopicInRedisTemplate(ChatRoom newChatRoom) {
        log.info("Setting topic in Redis template for chat room: {}", newChatRoom.getRoomId());
        ChannelTopic topic = new ChannelTopic("/sub/chat/room/" + newChatRoom.getRoomId());
        redisTopicTemplate.opsForValue().set("chatTopic:" + newChatRoom.getRoomId(), topic);
    }

    private ChatRoom validateChatRoom(String roomTitle, ChatRoom findChatRoom) {
        log.info("Validating chat room title.");
        if (findChatRoom.getTitle().isEmpty() || roomTitle != null) {
            log.info("Updating chat room title: {}", roomTitle);
            ChatRoom updateChatRoom = findChatRoom.toBuilder()
                    .title(roomTitle)
                    .build();

            findChatRoom = chatRoomRepository.save(updateChatRoom);
        }
        return findChatRoom;
    }

    private String getProfileImage(User findSender) {
        log.info("Getting profile image for user: {}", findSender.getUuid());
        return findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage();
    }

    private User findReceiver(Long receiver) {
        log.info("Finding receiver by ID: {}", receiver);
        return userRepository.findById(receiver).orElseThrow(
                () -> {
                    log.error("Receiver not found: {}", receiver);
                    return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
                }
        );
    }

    private User findSender(Long sender) {
        log.info("Finding sender by ID: {}", sender);
        return userRepository.findById(sender).orElseThrow(
                () -> {
                    log.error("Sender not found: {}", sender);
                    return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
                }
        );
    }

    @Override
    public String calculateTimeAgo(Timestamp lastTime) {
        log.info("Calculating time ago for timestamp: {}", lastTime);
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
        log.info("Fetching chat room list for user: {}", uuid);
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderOrReceiver(uuid, uuid);
        List<ChatRoomResponseDto> chatRoomList = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            log.info("Processing chat room: {}", room.getRoomId());
            User findSender = findSender(room.getOwner());
            User findReceiver = findReceiver(room.getMate());

            Timestamp lastTime = room.getLastTime();
            List<ChattingResponseDto> unreceivedMessages = getMessagesAfterLastTime(room.getRoomId(), lastTime, uuid);

            ChatRoomResponseDto chatroom = ChatRoomResponseDto.to(room, findSender, findReceiver, unreceivedMessages);

            chatRoomList.add(chatroom);
        }
        log.info("Chat room list fetched successfully.");
        return chatRoomList;
    }

    @Override
    public void saveChatToRedis(String roomId, ChattingResponseDto chat) {
        log.info("Saving chat to Redis. Room ID: {}, Chat: {}", roomId, chat);
        String key = "chatRoomId:" + roomId;

        List<ChattingResponseDto> chatList = loadMessageFromRedis(key);
        saveMessages(chat, chatList, key);
    }

    private void saveMessages(ChattingResponseDto chat, List<ChattingResponseDto> chatList, String key) {
        log.info("Checking for duplicate messages in Redis.");
        boolean isDuplicate = chatList.stream().anyMatch(savedChat ->
                savedChat.getLastTime().equals(chat.getLastTime()) &&
                        savedChat.getContent().equals(chat.getContent())
        );

        if (!isDuplicate) {
            log.info("Message is not a duplicate. Saving to Redis.");
            redisTemplateForSave.opsForList().rightPush(key, chat);
            redisTemplateForSave.expire(key, 3, TimeUnit.DAYS);
        } else {
            log.warn("Duplicate message detected. Not saving to Redis.");
        }
    }

    private List<ChattingResponseDto> loadMessageFromRedis(String key) {
        log.info("Fetching messages from Redis. Key: {}", key);
        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1);
        if (chatList == null) {
            log.info("No messages found in Redis for key: {}", key);
            chatList = new ArrayList<>();
        }
        return chatList;
    }

    @Override
    public void leaveRoom(Long roomId) {
        log.info("User leaving chat room: {}", roomId);
        chatRoomRepository.deleteById(roomId);
    }

    @Override
    public Timestamp getParsedLastTime(String lastTime) {
        log.info("Parsing timestamp: {}", lastTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", lastTime, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }

    @Override
    public List<ChattingResponseDto> getMessagesAfterLastTime(Long roomId, Timestamp lastTime, Long uuid) {
        log.info("Fetching messages after last time. Room ID: {}, Last Time: {}", roomId, lastTime);
        User findUser = getFindUser(uuid);

        // Redis에서 유저의 마지막 세션 종료 시간 가져오기
        log.info("유저의 마지막 세션 종료 시간 추출");
        Timestamp disconnectTime = getDisconnectTimeByUserId(lastTime, findUser);

        //채팅 가져오기
        String key = "chatRoomId:" + roomId;
        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1);

        if (chatList == null || chatList.isEmpty()) {
            log.warn("No messages found for roomId: {}", roomId);
            return new ArrayList<>();
        }

        List<ChattingResponseDto> unreceivedMessages = chatList.stream()
                .filter(message -> !message.getUserId().equals(findUser.getUuid()))
                .filter(message -> message.getLastTime().after(disconnectTime)) // 세션 종료 시간을 기준으로 필터링
                .sorted(Comparator.comparing(ChattingResponseDto::getLastTime).reversed()) //가장 최근 메세지가 위에 오도록
                .toList();

        log.info("Unreceived messages fetched for roomId: {}. Count: {}", roomId, unreceivedMessages.size());
        return unreceivedMessages;
    }

    private Timestamp getDisconnectTimeByUserId(Timestamp lastTime, User findUser) {
        String disconnectTimeKey = "session:lastDisconnected:" + findUser.getUuid();
        String disconnectTimeStr = redisTemplate.opsForValue().get(disconnectTimeKey);
        Timestamp disconnectTime;

        if (disconnectTimeStr != null) {
            disconnectTime = new Timestamp(Long.parseLong(disconnectTimeStr));
            log.info("User last disconnect time found: {}", disconnectTime);
        } else {
            log.warn("No disconnect time found for userId: {}. Using provided lastTime: {}", findUser.getUuid(), lastTime);
            disconnectTime = lastTime;
        }
        return disconnectTime;
    }

    private User getFindUser(Long uuid) {
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

    }

    @Override
    public void sendMessageToPublisher(ChattingRequestDto message) {
        log.info("{} send This Message - {}", message.getUserId(),message.getContent());
        log.info("message contain Image : {}", message.getImage());

        //1.해당 채팅방 조회
        ChatRoom findChatRoom = findChatRoom(message);

        //2.마지막 채팅 시간 추출
        Timestamp parsedLastTime = getParsedLastTime(message.getLastTime());

        // 메세지 받을 사람 지정
        // 오너가 보내면 메이트, 메이트가보내면 오너로 지정
        Long receiver = isMateOrOwner(message, findChatRoom);

        //유저가 세션에 참여중이지 않으면 fcm 알림을 보냄
        String sessionId = redisTemplate.opsForValue().get("user:session:" + receiver);
        if (sessionId == null || !webSocketEventListener.isSessionConnected(sessionId)) {
            log.warn("User {} is offline. Sending notification.", receiver);
            sendNotificationToDisConnectedUser(message, findChatRoom, parsedLastTime, receiver);
        }

        //채팅 응답 생성
        ChattingResponseDto responseDto = ChattingResponseDto.to(message, parsedLastTime);

        //응답 Redis에 저장
        saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

        //Pub에 내용을 퍼블리싱
        redisPublisher.publish(responseDto);
    }

    private static Long isMateOrOwner(ChattingRequestDto message, ChatRoom findChatRoom) {
        log.info("isMateOrOwner를 수행하여 받을 사람 지정");
        if(findChatRoom.getMate().equals(message.getUserId())) {
            log.info("receiver id = {}", findChatRoom.getOwner());
            return findChatRoom.getOwner();
        }else{
            log.info("receiver id = {}", findChatRoom.getMate());
            return findChatRoom.getMate();
        }
    }

    @Override
    public ChatRoomResponseDto getChatRoom(Long uuid, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ChatException(ExceptionCode.NOT_FOUND_CHATROOM)
        );

        log.info("Processing chat room: {}", chatRoom.getRoomId());
        User findSender = findSender(chatRoom.getOwner());
        User findReceiver = findReceiver(chatRoom.getMate());

        Timestamp lastTime = chatRoom.getLastTime();
        List<ChattingResponseDto> unreceivedMessages = getMessagesAfterLastTime(chatRoom.getRoomId(), lastTime, uuid);

        return ChatRoomResponseDto.to(chatRoom, findSender, findReceiver, unreceivedMessages);
    }

    private void sendNotificationToDisConnectedUser(ChattingRequestDto message, ChatRoom findChatRoom, Timestamp parsedLastTime, Long receiver) {
        log.info("Sending notification to disconnected user: {}", receiver);
        NotificationRequestDto notificationRequestDto = NotificationRequestDto.to(message, findChatRoom, parsedLastTime, receiver);
        try {
            fcmServiceImpl.sendNotification(notificationRequestDto);
            log.info("Notification sent successfully to user: {}", receiver);
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", receiver, e);
            throw new ChatException(ExceptionCode.INTERRUPTION_OR_EXECUTION_ERR);
        }
    }

    private ChatRoom findChatRoom(ChattingRequestDto message) {
        log.info("Finding chat room by ID: {}", message.getRoomId());
        return chatRoomRepository.findById(message.getRoomId()).orElseThrow(
                () -> {
                    log.error("Chat room not found: {}", message.getRoomId());
                    return new ChatException(ExceptionCode.NOT_FOUND_CHATROOM);
                }
        );
    }
}
