package pupket.togedogserver.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(Long roomId);

    List<ChatRoom> findByOwner_User_Uuid(Long uuid);

    List<ChatRoom> findByMate_User_Uuid(Long uuid);
}
