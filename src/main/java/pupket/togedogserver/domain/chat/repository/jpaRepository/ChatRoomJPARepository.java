package pupket.togedogserver.domain.chat.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomJPARepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findBySender(Long uuid);

    Optional<ChatRoom> findBySenderAndReceiverAndTitle(Long sender, Long receiver, String title);

    Optional<ChatRoom> findBySenderAndReceiverAndTitleOrReceiverAndSenderAndTitle(Long sender, Long receiver, String roomTitle, Long receiver1, Long sender1, String roomTitle1);

    List<ChatRoom> findBySenderOrReceiver(Long uuid, Long uuid1);
}
