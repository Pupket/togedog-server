package pupket.togedogserver.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.chat.entity.ChatRoom;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Date;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class Board {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long photoCount = 0L;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date editedAt;

    @Column(nullable = false)
    private Date startTime;

    @Column(nullable = false)
    private Date endTime;

    /**
     * 도로명 주소
     */
    private String pickupLocation1;

    /**
     * 세부 주소
     */
    private String pickupLocation2;

    private String walkingLocation;

    @Column(nullable = false)
    private Long fee;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'UNMATCHED'")
    @Column(nullable = false)
    private MatchStatus matched = MatchStatus.UNMATCHED;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted = Boolean.FALSE;

    @ManyToOne
    private User user;

    @OneToMany
    private List<OwnerBoard> ownerBoard;

    @OneToMany
    private List<MateBoard> mateBoard;

    @OneToMany
    private List<ChatRoom> chatRoom;

    @OneToMany
    private List<BoardPhoto> boardPhoto;
}
