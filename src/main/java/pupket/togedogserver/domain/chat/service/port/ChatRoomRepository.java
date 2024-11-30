package pupket.togedogserver.domain.chat.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository {
    List<ChatRoom> findBySender(Long uuid);

    Optional<ChatRoom> findBySenderAndReceiverAndTitle(Long sender, Long receiver, String title);

    Optional<ChatRoom> findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(Long sender, Long receiver, String roomTitle, Long receiver1, Long sender1, String roomTitle1);

    List<ChatRoom> findBySenderOrReceiver(Long uuid, Long uuid1);

    Optional<ChatRoom> findById(Long roomId);

    void deleteById(Long roomId);

    ChatRoom save(ChatRoom updateChatRoom);
}
