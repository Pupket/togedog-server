package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import pupket.togedogserver.domain.dog.entity.Dog;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class OwnerBoard {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long ownerBoardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id")
    private Dog dog;
}
