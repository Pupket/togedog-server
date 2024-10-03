package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.ChatRoomRepository;
import pupket.togedogserver.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public Long createChatRoom(Long sender, Long receiver) {
        ChatRoom chatRoom = ChatRoom.chatRoomUser()
                .user1(userRepository.findByUuid(sender).get().getUuid())
                .user2(userRepository.findByUuid(receiver).get().getUuid())
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    public List<Long> getChatRoomList(Long uuid) {
        List<Long> ownerChatRoomList = getUser1ChatRoomList(uuid);
        List<Long> mateChatRoomList = getUser2ChatRoomList(uuid);
        List<Long> chatRoomList = new ArrayList<>(ownerChatRoomList);
        chatRoomList.addAll(mateChatRoomList);
        return chatRoomList;
    }

    public List<Long> getUser1ChatRoomList(Long uuid) {
        return chatRoomRepository.findByUser1(uuid)
                .stream()
                .map(ChatRoom::getRoomId)
                .toList();
    }

    public List<Long> getUser2ChatRoomList(Long uuid) {
        return chatRoomRepository.findByUser2(uuid)
                .stream()
                .map(ChatRoom::getRoomId)
                .toList();
    }
}
