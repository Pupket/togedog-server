package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long roomId;

    private Long owner;

    private Long mate;

    private String title;

    private Timestamp lastTime;

    private String senderImage;

    private String receiverImage;

    public static ChatRoom to(Long mate, Long owner, String findSenderProfileImage, String roomTitle, String findReceiverProfileImage) {
        return ChatRoom.builder()
                .mate(mate)
                .owner(owner)
                .senderImage(findSenderProfileImage)
                .title(roomTitle)
                .receiverImage(findReceiverProfileImage)
                .lastTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();

    }
}
