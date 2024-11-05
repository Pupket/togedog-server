package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChatRoomCreateResponse;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;
import pupket.togedogserver.domain.notification.service.FcmService;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.ChatException;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;
import pupket.togedogserver.global.websocket.WebSocketEventListener;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, ChattingResponseDto> redisTemplateForSave;
    private final RedisTemplate<String, String> redisTemplateForUserStatus;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final RedisTemplate<String, ChannelTopic> redisTopicTemplate;
    private final S3FileUtilImpl s3FileUtilImpl;
    private final RedisPublisher redisPublisher;
    private final WebSocketEventListener webSocketEventListener;

    public ChatRoomCreateResponse getOrCreateChatRoom(Long sender, Long receiver, String roomTitle) {
        User findSender = userRepository.findById(sender).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        User findReceiver = userRepository.findById(receiver).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        String findSenderProfileImage = findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage();
        String findReceiverProfileImage = findReceiver.getProfileImage().isEmpty() ? null : findReceiver.getProfileImage();

        ChatRoom findChatRoom = chatRoomRepository.findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(sender, receiver, roomTitle, receiver, sender, roomTitle)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .receiver(receiver)
                            .sender(sender)
                            .senderImage(findSenderProfileImage)
                            .title(roomTitle)
                            .receiverImage(findReceiverProfileImage)
                            .lastTime(Timestamp.valueOf(LocalDateTime.now()))
                            .build();

                    chatRoomRepository.save(newChatRoom);
                    ChannelTopic topic = new ChannelTopic("/sub/chat/room/" + newChatRoom.getRoomId());
                    redisTopicTemplate.opsForValue().set("chatTopic:" + newChatRoom.getRoomId(), topic);
                    return newChatRoom;
                });

        if (findChatRoom.getTitle().isEmpty() && roomTitle != null) {
            ChatRoom updateChatRoom = findChatRoom.toBuilder()
                    .title(roomTitle)
                    .build();

            findChatRoom = chatRoomRepository.save(updateChatRoom);
        }

        if(findChatRoom.getSender().equals(findSender.getUuid())) {
            return  ChatRoomCreateResponse.builder()
                    .roomTitle(findChatRoom.getTitle())
                    .roomId(findChatRoom.getRoomId())
                    .nickName(null)
                    .build();
        }

        return ChatRoomCreateResponse.builder()
                .roomTitle(null)
                .roomId(findChatRoom.getRoomId())
                .nickName(findReceiver.getNickname())
                .build();
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
            User findSender = userRepository.findByUuid(room.getReceiver())
                    .orElseThrow(() -> new MateException(ExceptionCode.NOT_FOUND_MEMBER));
            User findReceiver = userRepository.findByUuid(room.getSender()).orElseThrow(
                    () -> new MateException(ExceptionCode.NOT_FOUND_MEMBER)
            );
            Timestamp lastTime = room.getLastTime();
            List<ChattingResponseDto> unreceivedMessages = getMessagesAfterLastTime(room.getRoomId(), lastTime);

            int unreceivedMessageCount = unreceivedMessages.size();
            String lastMessage = unreceivedMessages.isEmpty() ? null : unreceivedMessages.get(0).getContent();

            ChatRoomResponseDto chatroom = ChatRoomResponseDto.builder()
                    .roomId(room.getRoomId())
                    .lastTime(room.getLastTime())
                    .title(room.getTitle())
                    .sender(findSender.getNickname())
                    .senderImage(findSender.getProfileImage().isEmpty() ? null : findSender.getProfileImage())
                    .receiver(findReceiver.getNickname())
                    .receiverImage(findReceiver.getProfileImage().isEmpty() ? null : findReceiver.getProfileImage())
                    .unreceivedMessageCount(unreceivedMessageCount)
                    .lastMessage(lastMessage)
                    .build();

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
        String key = "RoomId:" + roomId;

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

    public String convertImageToString(String image) throws IOException {
        return s3FileUtilImpl.uploadImageToS3UsingByteImage(image);
    }

    public void sendMessageToPublisher(ChattingRequestDto message) {

        Timestamp parsedLastTime = getParsedLastTime(message.getLastTime());
        ChatRoom findChatRoom = chatRoomRepository.findById(message.getRoomId()).orElseThrow(
                () -> new ChatException(ExceptionCode.NOT_FOUND_CHATROOM)
        );

        Long reciever = findChatRoom.getReceiver();
        String receiverStatus = redisTemplateForUserStatus.opsForValue().get("user:status:" + reciever);
        if (!webSocketEventListener.isSessionConnected(receiverStatus)) {
            NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                    .message(message.getContent())
                    .title(findChatRoom.getTitle())
                    .receiver(findChatRoom.getReceiver())
                    .image(message.getImage())
                    .roomId(findChatRoom.getRoomId())
                    .build();
            try {
                fcmService.sendNotification(notificationRequestDto, findChatRoom.getRoomId());
            } catch (Exception e) {
                throw new ChatException(ExceptionCode.INTERRUPTION_OR_EXECUTION_ERR);
            }
        }

        ChattingResponseDto responseDto = ChattingResponseDto.builder()
                .lastTime(parsedLastTime)
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .content(message.getContent())
                .image(message.getImage())
                .build();

        // Redis에 메시지 저장
        saveChatToRedis(String.valueOf(message.getRoomId()), responseDto);

        // 메시지 발행
        redisPublisher.publish(responseDto);
    }
}