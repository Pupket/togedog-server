package pupket.togedogserver.domain.user.entity.mate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.user.constant.Time;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MatePreferredTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matePreferredTimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_id")
    private Mate mate;

    @Enumerated(EnumType.STRING)
    private Time preferredTime;
}
