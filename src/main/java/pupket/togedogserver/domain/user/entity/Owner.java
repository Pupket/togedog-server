package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.match.entity.Match;

import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Owner {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long ownerUuid;

    @OneToOne(mappedBy = "owner", fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Long matchCount = 0L;

    @OneToMany(mappedBy = "owner")
    private List<Match> match;
}
