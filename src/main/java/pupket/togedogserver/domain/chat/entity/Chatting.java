package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class Chatting {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private String imageURL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uuid")
    private User user;

    private Date writtenTime;
}
