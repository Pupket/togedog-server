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

    @OneToOne
    private User user;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long matchCount = 0L;

    @Column(columnDefinition = "LONGTEXT")
    private String introduce;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ChatRoom> chatRoom;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Match> match;
}
