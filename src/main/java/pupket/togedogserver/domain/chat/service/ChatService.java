package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.chat.repository.ChatRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    public List<Chatting> getChats(Long roomId) {
        return chatRepository.findByChatRoom_RoomId(roomId);
    }

    public void saveChatting(Chatting chatting) {
        chatRepository.save(chatting);
    }

}
