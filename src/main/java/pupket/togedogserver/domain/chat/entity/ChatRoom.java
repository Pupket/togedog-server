package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onwer_uuid")
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_uuid")
    private Mate mate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "chatRoom")
    private List<Chatting> chatting;

    public ChatRoom createChatRoom(Owner owner, Mate mate, Board board) {
        ChatRoom chatRoom = new ChatRoom();
        this.owner = owner;
        this.mate = mate;
        this.board = board;
        return chatRoom;
    }
}
