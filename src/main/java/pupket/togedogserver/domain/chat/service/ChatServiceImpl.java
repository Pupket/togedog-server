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
import pupket.togedogserver.global.s3.util.S3FileUtil;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.websocket.WebSocketEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        log.info("Getting or creating chat room. Sender: {}, Receiver: {}, RoomTitle: {}", sender, receiver, roomTitle);

        User findSender = findSender(sender);
        User findReceiver = findReceiver(receiver);

        log.debug("Sender found: {}. Receiver found: {}", findSender, findReceiver);

        String findSenderProfileImage = getProfileImage(findSender);
        String findReceiverProfileImage = getProfileImage(findReceiver);

        ChatRoom findChatRoom = createChatRoom(sender, receiver, roomTitle, findSenderProfileImage, findReceiverProfileImage);

        findChatRoom = validateChatRoom(roomTitle, findChatRoom);

        log.debug("Chat room created or validated: {}", findChatRoom);

        if (findChatRoom.getSender().equals(findSender.getUuid())) {
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
        log.debug("Creating chat room if not exists. Sender: {}, Receiver: {}, Title: {}", sender, receiver, roomTitle);
        ChatRoom findChatRoom = chatRoomRepository.findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(sender, receiver, roomTitle, receiver, sender, roomTitle)
                .orElseGet(() -> {
                    log.info("No existing chat room found. Creating a new one.");
                    ChatRoom newChatRoom = ChatRoom.to(receiver, sender, findSenderProfileImage, roomTitle, findReceiverProfileImage);

                    chatRoomRepository.save(newChatRoom);
                    log.info("New chat room saved: {}", newChatRoom);

                    setTopicInRedisTemplate(newChatRoom);
                    return newChatRoom;
                });
        return findChatRoom;
    }

    private void setTopicInRedisTemplate(ChatRoom newChatRoom) {
        log.debug("Setting topic in Redis template for chat room: {}", newChatRoom.getRoomId());
        ChannelTopic topic = new ChannelTopic("/sub/chat/room/" + newChatRoom.getRoomId());
        redisTopicTemplate.opsForValue().set("chatTopic:" + newChatRoom.getRoomId(), topic);
    }

    private ChatRoom validateChatRoom(String roomTitle, ChatRoom findChatRoom) {
        log.debug("Validating chat room title.");
        if (findChatRoom.getTitle().isEmpty() && roomTitle != null) {
            log.info("Updating chat room title: {}", roomTitle);
            ChatRoom updateChatRoom = findChatRoom.toBuilder()
                    .title(roomTitle)
                    .build();

            findChatRoom = chatRoomRepository.save(updateChatRoom);
        }
        return findChatRoom;
    }

    private String getProfileImage(User findSender) {
        log.debug("Getting profile image for user: {}", findSender.getUuid());
        return findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage();
    }

    private User findReceiver(Long receiver) {
        log.debug("Finding receiver by ID: {}", receiver);
        return userRepository.findById(receiver).orElseThrow(
                () -> {
                    log.error("Receiver not found: {}", receiver);
                    return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
                }
        );
    }

    private User findSender(Long sender) {
        log.debug("Finding sender by ID: {}", sender);
        return userRepository.findById(sender).orElseThrow(
                () -> {
                    log.error("Sender not found: {}", sender);
                    return new MemberException(ExceptionCode.NOT_FOUND_MEMBER);
                }
        );
    }

    @Override
    public String calculateTimeAgo(Timestamp lastTime) {
        log.debug("Calculating time ago for timestamp: {}", lastTime);
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
            log.debug("Processing chat room: {}", room.getRoomId());
            User findSender = findSender(room.getSender());
            User findReceiver = findReceiver(room.getReceiver());

            Timestamp lastTime = room.getLastTime();
            List<ChattingResponseDto> unreceivedMessages = getMessagesAfterLastTime(room.getRoomId(), lastTime, uuid);

            ChatRoomResponseDto chatroom = ChatRoomResponseDto.to(room, findSender, findReceiver, unreceivedMessages);

            chatRoomList.add(chatroom);
        }
        log.info("Chat room list fetched successfully.");
        return chatRoomList;
    }

    public void saveChatToRedis(String roomId, ChattingResponseDto chat) {
        log.debug("Saving chat to Redis. Room ID: {}, Chat: {}", roomId, chat);
        String key = "chatRoomId:" + roomId;

        List<ChattingResponseDto> chatList = loadMessageFromRedis(key);
        saveMessages(chat, chatList, key);
    }

    private void saveMessages(ChattingResponseDto chat, List<ChattingResponseDto> chatList, String key) {
        log.debug("Checking for duplicate messages in Redis.");
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
        log.debug("Fetching messages from Redis. Key: {}", key);
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
        log.debug("Parsing timestamp: {}", lastTime);
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
        log.debug("Fetching messages after last time. Room ID: {}, Last Time: {}", roomId, lastTime);
        User findUser = userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        String key = "chatRoomId:" + roomId;

        List<ChattingResponseDto> chatList = redisTemplateForSave.opsForList().range(key, 0, -1);

        if (chatList == null || chatList.isEmpty()) {
            log.warn("No messages found for roomId: {}", roomId);
            return new ArrayList<>();
        }

        List<ChattingResponseDto> unreceivedMessages = chatList.stream()
                .filter(message -> message.getLastTime().after(lastTime))
                .filter(message -> !message.getUserId().equals(findUser.getUuid()))
                .sorted(Comparator.comparing(ChattingResponseDto::getLastTime).reversed())
                .collect(Collectors.toList());

        log.info("Unreceived messages fetched for roomId: {}. Count: {}", roomId, unreceivedMessages.size());
        return unreceivedMessages;
    }

    @Override
    public void sendMessageToPublisher(ChattingRequestDto message) {
        log.info("Sending message to publisher: {}", message);
        Timestamp parsedLastTime = getParsedLastTime(message.getLastTime());
        ChatRoom findChatRoom = findChatRoom(message);

        Long receiver = findChatRoom.getReceiver().equals(message.getUserId()) ? findChatRoom.getSender() : findChatRoom.getReceiver();
        log.info("Receiver determined: {}", receiver);

        String sessionId = redisTemplateForUserStatus.opsForValue().get("user:session:" + receiver);
        if (sessionId == null || !webSocketEventListener.isSessionConnected(sessionId)) {
            log.warn("User {} is offline. Sending notification.", receiver);
            sendNotificationToDisConnectedUser(message, findChatRoom, parsedLastTime, receiver);
        }

        ChattingResponseDto responseDto = ChattingResponseDto.to(message, parsedLastTime);

        saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

        redisPublisher.publish(responseDto);
    }

    private void sendNotificationToDisConnectedUser(ChattingRequestDto message, ChatRoom findChatRoom, Timestamp parsedLastTime, Long receiver) {
        log.debug("Sending notification to disconnected user: {}", receiver);
        NotificationRequestDto notificationRequestDto = NotificationRequestDto.to(message, findChatRoom, parsedLastTime, receiver);
        try {
            fcmServiceImpl.sendNotification(notificationRequestDto, receiver);
            log.info("Notification sent successfully to user: {}", receiver);
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", receiver, e);
            throw new ChatException(ExceptionCode.INTERRUPTION_OR_EXECUTION_ERR);
        }
    }

    private ChatRoom findChatRoom(ChattingRequestDto message) {
        log.debug("Finding chat room by ID: {}", message.getRoomId());
        return chatRoomRepository.findById(message.getRoomId()).orElseThrow(
                () -> {
                    log.error("Chat room not found: {}", message.getRoomId());
                    return new ChatException(ExceptionCode.NOT_FOUND_CHATROOM);
                }
        );
    }
}
