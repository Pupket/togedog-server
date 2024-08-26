package pupket.togedogserver.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "matching")
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Match {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long matchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'UNMATCHED'")
    @Builder.Default
    private MatchStatus matched = MatchStatus.UNMATCHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'INCOMPLETE'")
    @Builder.Default
    private CompleteStatus completeStatus = CompleteStatus.INCOMPLETE;

    private String review;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_uuid")
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_uuid")
    private Mate mate;

    @OneToOne(fetch = FetchType.LAZY)
    private Board board;
}
