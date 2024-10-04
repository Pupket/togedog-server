package pupket.togedogserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.sql.Timestamp;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Builder(builderMethodName = "chatRoomUser")
    public ChatRoom(Long user1, Long user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long roomId;

    private Long user1;

    private Long user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_uuid")
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_uuid")
    private Mate mate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
}
