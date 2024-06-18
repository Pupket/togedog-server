package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    private Board board;

    @ManyToOne
    private Dog dog;
}
