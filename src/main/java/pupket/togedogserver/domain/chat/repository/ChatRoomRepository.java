package pupket.togedogserver.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findBySender(Long uuid);

    Optional<ChatRoom> findBySenderAndReceiver(Long sender, Long receiver);
}
