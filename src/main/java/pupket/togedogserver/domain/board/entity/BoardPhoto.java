package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    private Board board;
}
