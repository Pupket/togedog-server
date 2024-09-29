package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.chat.repository.ChatRepository;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<Chatting> getChats(CustomUserDetail userDetail, Long roomId) {
        return chatRepository.findByChatRoom_RoomId(roomId);
    }

    public void saveChatting(Chatting chatting) {
        chatRepository.save(chatting);
    }

    public List<Long> getOwnerChatRoomList(Long uuid) {
        return chatRoomRepository.findByOwner_User_Uuid(uuid)
                .stream()
                .map(ChatRoom::getRoomId)
                .toList();
    }

    public List<Long> getMateChatRoomList(Long uuid) {
        return chatRoomRepository.findByMate_User_Uuid(uuid)
                .stream()
                .map(ChatRoom::getRoomId)
                .toList();
    }
}
