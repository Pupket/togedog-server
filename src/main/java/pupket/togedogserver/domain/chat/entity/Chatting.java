package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
public class Chatting {

    @Builder(builderMethodName = "userchat")
    public Chatting(ChatRoom chatRoom, String content, String imageURL, User sender, User receiver, Date writtenTime) {
        this.chatRoom = chatRoom;
        this.content = content;
        this.imageURL = imageURL;
        this.sender = sender;
        this.receiver = receiver;
        this.writtenTime = writtenTime;
    }

    @Builder(builderMethodName = "serverchat")
    public Chatting(ChatRoom chatRoom, String content, Date writtenTime) {
        this.chatRoom = chatRoom;
        this.content = content;
        this.writtenTime = writtenTime;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId")
    private ChatRoom chatRoom;

    private String content;

    private String imageURL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uuid", insertable = false, updatable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uuid", insertable = false, updatable = false)
    private User receiver;

    private Date writtenTime;

}
