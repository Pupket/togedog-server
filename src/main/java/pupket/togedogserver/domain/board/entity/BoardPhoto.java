package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class BoardPhoto {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long photoURL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
}
