package pupket.togedogserver.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.Chatting;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chatting, Long> {
    Chatting findByChatId(Long chatId);

    List<Chatting> findByChatRoom_RoomId(Long roomId);

    boolean existsByChatRoom_RoomId(Long roomId);
}
