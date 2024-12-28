package pupket.togedogserver.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.chat.repository.jpaRepository.ChatRoomJPARepository;
import pupket.togedogserver.domain.chat.service.port.ChatRoomRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {
    private final ChatRoomJPARepository chatRoomJPARepository;

    @Override
    public Optional<ChatRoom> findByOwnerAndMateAndTitle(Long sender, Long receiver, String roomTitle, Long receiver1, Long sender1, String roomTitle1) {
        return chatRoomJPARepository.findBySenderAndReceiverAndTitle(sender, receiver, roomTitle);
    }

    @Override
    public List<ChatRoom> findByOwnerOrMate(Long uuid, Long uuid1) {
        return chatRoomJPARepository.findBySenderOrReceiver(uuid, uuid1);
    }

    @Override
    public Optional<ChatRoom> findById(Long roomId) {
        return chatRoomJPARepository.findById(roomId);
    }

    @Override
    public void deleteById(Long roomId) {
        chatRoomJPARepository.deleteById(roomId);
    }

    @Override
    public ChatRoom save(ChatRoom updateChatRoom) {
        return chatRoomJPARepository.save(updateChatRoom);
    }
}
