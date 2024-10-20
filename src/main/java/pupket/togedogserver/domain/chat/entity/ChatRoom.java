package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Builder(builderMethodName = "chatRoomUser")
    public ChatRoom(Long sender, Long receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.lastTime = new Timestamp(System.currentTimeMillis());
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long roomId;

    private Long sender;

    private Long receiver;

    private Timestamp lastTime;
}
