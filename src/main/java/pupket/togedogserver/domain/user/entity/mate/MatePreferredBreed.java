package pupket.togedogserver.domain.user.entity.mate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.dog.constant.Breed;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatePreferredBreed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matePreferredBreedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Mate mate;

    @Enumerated(EnumType.STRING)
    private Breed preferredBreed;
}
