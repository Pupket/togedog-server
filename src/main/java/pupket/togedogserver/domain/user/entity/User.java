package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.constant.AccountStatus;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Visibility;

import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "users")
@Getter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGender userGender;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'HIDDEN'")
    @Column(nullable = false)
    private Visibility genderVisibility = Visibility.HIDDEN;

    /**
     * 도로명 주소
     */
    @Column(nullable = false)
    private String address1;

    /**
     * 세부 주소
     */
    private String address2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    public AccountStatus accountStatus = AccountStatus.ACTIVE;

    @OneToOne
    private Owner owner;

    @OneToOne
    private Mate mate;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Dog> dog;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Board> board;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Chatting> chatting;
}
