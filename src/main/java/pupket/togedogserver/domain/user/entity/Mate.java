package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.match.entity.Match;

import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class Mate {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long mateUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uuid")
    private User user;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long matchCount = 0L;

    @Column(columnDefinition = "LONGTEXT")
    private String introduce;

    @OneToMany(mappedBy = "mate")
    private List<ChatRoom> chatRoom;

    @OneToMany(mappedBy = "mate")
    private List<Match> match;
}
