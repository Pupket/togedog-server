package pupket.togedogserver.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.chat.repository.ChatRepository;
import pupket.togedogserver.domain.notification.service.NotificationServiceImpl;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final NotificationServiceImpl notificationService;

    public List<Chatting> getChats(CustomUserDetail userDetail, Long roomId) {
        return chatRepository.findByChatRoom_RoomId(roomId);
    }

    public void saveChatting(Chatting chatting) {
        chatRepository.save(chatting);
    }

}
