package pupket.togedogserver.domain.user.entity.mate;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE mate SET deleted = true WHERE mate_uuid = ?")
@Where(clause = "deleted = false")
public class Mate {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long mateUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uuid")
    private User user;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Long matchCount = 0L;

    @Column(columnDefinition = "LONGTEXT")
    private String career;

    @Column(nullable = false)
    private int accommodatableDogsCount;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @OneToMany(mappedBy = "mate")
    private List<ChatRoom> chatRoom;

    @OneToMany(mappedBy = "mate")
    private List<Match> match;

    @OneToMany(mappedBy = "mate", fetch = FetchType.LAZY)
    private Set<MatePreferredBreed> preferredBreeds;

    @OneToMany(mappedBy = "mate", fetch = FetchType.LAZY)
    private Set<MatePreferredTime> preferredTimes;

    @OneToMany(mappedBy = "mate", fetch = FetchType.LAZY)
    private Set<MatePreferredWeek> preferredWeeks;

    @OneToMany(mappedBy = "mate", fetch = FetchType.LAZY)
    private Set<MateTag> mateTags;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Enumerated(EnumType.STRING)
    private Region preferredRegion;
}
