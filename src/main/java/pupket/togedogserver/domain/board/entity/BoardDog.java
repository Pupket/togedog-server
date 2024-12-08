package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import pupket.togedogserver.domain.dog.entity.Dog;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE board_dog SET deleted = true WHERE board_dog_id = ?")
@SQLRestriction("deleted = false")
public class BoardDog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardDogId;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "dog_id")
    private Dog dog;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean deleted = Boolean.FALSE;

}
