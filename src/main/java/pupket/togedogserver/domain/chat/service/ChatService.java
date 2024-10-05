package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

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

}
