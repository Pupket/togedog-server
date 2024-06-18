package pupket.togedogserver.domain.match.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.entity.Mate;
import pupket.togedogserver.domain.user.entity.Owner;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "matching")
@Getter
@ToString
public class Match {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long matchId;

    @Column(nullable = false)
    private Date startTime;

    @Column(nullable = false)
    private Date endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'UNMATCHED'")
    private MatchStatus matched = MatchStatus.UNMATCHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'INCOMPLETE'")
    private CompleteStatus completeStatus = CompleteStatus.INCOMPLETE;

    private String review;

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE;

    @ManyToOne
    private Owner owner;

    @ManyToOne
    private Mate mate;
}
