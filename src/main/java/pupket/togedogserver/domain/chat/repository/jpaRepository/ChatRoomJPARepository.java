package pupket.togedogserver.domain.chat.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomJPARepository extends JpaRepository<ChatRoom, Long> {


    Optional<ChatRoom> findBySenderAndReceiverAndTitle(Long sender, Long receiver, String title);


    List<ChatRoom> findBySenderOrReceiver(Long uuid, Long uuid1);
}
