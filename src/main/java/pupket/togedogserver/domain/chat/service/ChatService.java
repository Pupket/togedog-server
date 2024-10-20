package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.domain.user.repository.mateRepo.MateRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    private final RedisTemplate<String, List<ChattingResponseDto>> redisTemplateForResponse;
    private final RedisTemplate<String, List<ChattingRequestDto>> redisTemplateForRequests;
    private final MateRepository mateRepository;
    private final UserRepository userRepository;

    //채팅방 생성
    public Long createChatRoom(Long sender, Long receiver) {
        ChatRoom chatRoom = new ChatRoom(sender, receiver);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    //채팅방 조회
    public List<ChatRoomResponseDto> getChatRoomList(Long uuid) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySender(uuid);

        List<ChatRoomResponseDto> chatRoomList = new ArrayList<>();
        for (ChatRoom room : chatRooms) {
            ChatRoomResponseDto chatroom = new ChatRoomResponseDto();
            chatroom.setRoomId(room.getRoomId());
            chatroom.setLastTime(room.getLastTime());
            chatroom.setSender(userRepository.findById(room.getSender()).orElseThrow(
                            () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
                    ).getNickname()
            );
            chatRoomList.add(chatroom);
        }

        return chatRoomList;
    }

    //미수신 채팅 가져오기
    public List<ChattingResponseDto> getUndeliveredChats(Long room, Timestamp lastTime) {
        List<ChattingResponseDto> recentChats = getRecentChats(room);
        if (recentChats == null || recentChats.isEmpty()) {
            log.warn("No recent chats found for room: {}", room);
            return new ArrayList<>();  // 데이터가 없을 경우 빈 리스트 반환
        }
        return separateUndeliveredChats(recentChats, lastTime);
    }

    //최근 채팅 가져오기
    private List<ChattingResponseDto> getRecentChats(Long room) {
        return redisTemplateForResponse.opsForValue().get("chatRoomId:" + room);
    }

    private List<ChattingResponseDto> separateUndeliveredChats(List<ChattingResponseDto> recentChats, Timestamp lastTime) {
        List<ChattingResponseDto> undeliveredChats = new ArrayList<>();

        if (recentChats != null) {
            for (ChattingResponseDto chat : recentChats) {
                Timestamp lastTime1 = chat.getLastTime();
                if (lastTime1.after(lastTime)) {
                    undeliveredChats.add(chat);
                }
            }
        }

        return undeliveredChats;
    }

    // Redis에 사용자 모든 채팅방을 백업하는 메서드
    public void backupChats(Long uuid, List<ChattingRequestDto> chats) {
        // 사용자 uuid가 포함된 모든 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findBySender(uuid);

        // 각 채팅방의 ID를 기준으로 Redis에 채팅 저장
        for (ChatRoom chatRoom : chatRooms) {
            String key = "room:" + chatRoom.getRoomId() + ":chat_backup";
            try {
                List<ChattingRequestDto> existingChats = redisTemplateForRequests.opsForValue().get(key);

                if (existingChats == null) {
                    existingChats = new ArrayList<>();
                }

                // 중복 검사: 이미 존재하는 채팅과 동일한 content와 lastTime을 가진 채팅은 추가하지 않음
                for (ChattingRequestDto newChat : chats) {
                    boolean isDuplicate = existingChats.stream()
                            .anyMatch(existingChat ->
                                    existingChat.getContent().equals(newChat.getContent()) &&
                                            existingChat.getLastTime().equals(newChat.getLastTime()));

                    if (!isDuplicate) {
                        existingChats.add(newChat);
                    }
                }

                redisTemplateForRequests.opsForValue().set(key, existingChats, 3, TimeUnit.DAYS);  // 3일간 보관
            } catch (Exception e) {
                log.error("Failed to backup chats for room: " + chatRoom.getRoomId(), e);
            }
        }
    }

    //방 나가기(삭제)
    public void leaveRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }

    private Timestamp getParsedLastTime(String lastTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(lastTime).getTime());
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }
}
