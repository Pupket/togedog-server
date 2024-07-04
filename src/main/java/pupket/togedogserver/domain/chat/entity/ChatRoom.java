package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.Mate;
import pupket.togedogserver.domain.user.entity.Owner;

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
    @JoinColumn(name = "onwer_id")
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onwer_uuid")
    private Mate mate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "chatRoom")
    private List<Chatting> chatting;
}
