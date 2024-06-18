package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    private ChatRoom chatRoom;

    private String imageURL;

    @ManyToOne
    private User user;

    private Date writtenTime;
}
