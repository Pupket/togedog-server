package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.sql.Timestamp;

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

    private Long sender;

    private Long receiver;

    private String title;

    private Timestamp lastTime;

    private String senderImage;

    private String receiverImage;
}
