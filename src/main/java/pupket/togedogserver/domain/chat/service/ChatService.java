package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    private final RedisTemplate<String, List<ChattingResponseDto>> redisTemplate;

    public Long createChatRoom(Long sender, Long receiver) {
        ChatRoom chatRoom = ChatRoom.chatRoomUser()
                .user1(userRepository.findByUuid(sender).get().getUuid())
                .user2(userRepository.findByUuid(receiver).get().getUuid())
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    public List<ChatRoomResponseDto> getChatRoomList(Long uuid) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1(uuid);
        chatRooms.addAll(chatRoomRepository.findByUser2(uuid));

        List<ChatRoomResponseDto> chatRoomList = new ArrayList<>();
        for (ChatRoom room : chatRooms) {
            ChatRoomResponseDto chatroom = new ChatRoomResponseDto();
            chatroom.setRoomId(room.getRoomId());
            chatroom.setContent(room.getContent());
            chatroom.setLastTime(room.getLastTime());
            chatroom.setNickname(room.getNickname());
            chatRoomList.add(chatroom);
        }

        return chatRoomList;
    }

    public List<ChattingResponseDto> getUndeliveredChats(Long room, Timestamp lastTime) {
        List<ChattingResponseDto> recentChats = getRecentChats(room);
        return separateUndeliveredChats(recentChats, lastTime);
    }

    private List<ChattingResponseDto> getRecentChats(Long room) {
        return redisTemplate.opsForValue().get("room: " + room);
    }

    private List<ChattingResponseDto> separateUndeliveredChats(List<ChattingResponseDto> recentChats, Timestamp lastTime) {
        List<ChattingResponseDto> undeliveredChats = new ArrayList<>();

        if (recentChats != null) {
            for (ChattingResponseDto chat : recentChats) {
                if (chat.getLastTime().after(lastTime)) {
                    undeliveredChats.add(chat);
                }
            }
        }

        return undeliveredChats;
    }

    public void backupChats(Long uuid, List<ChattingRequestDto> chats) {
        // todo:: 기기 변경 시에 사용할 채팅방 백업 기능
    }

    public void leaveRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }

}
