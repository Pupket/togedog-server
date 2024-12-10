package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.match.entity.Match;

import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
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

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "owner")
    private List<Match> match;
}
