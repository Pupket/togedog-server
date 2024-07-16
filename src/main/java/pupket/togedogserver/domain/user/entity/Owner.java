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
public class Owner {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long ownerUuid;

    @OneToOne(mappedBy = "owner", fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long matchCount = 0L;

    @Column(columnDefinition = "LONGTEXT")
    private String introduce;

    @OneToMany(mappedBy = "owner")
    private List<ChatRoom> chatRoom;

    @OneToMany(mappedBy = "owner")
    private List<Match> match;
}
