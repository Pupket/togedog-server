package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.constant.AccountStatus;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Visibility;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "users")
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET account_status = 'DELETED' WHERE uuid = ?")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private String email;

    private String password;

    private String nickname;

    private String profileImage;

    private String name;

    @Enumerated(EnumType.STRING)
    private UserGender userGender;

    private int birthyear;

    private int birthday;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'HIDDEN'")
    @Column(nullable = false)
    @Builder.Default
    private Visibility genderVisibility = Visibility.HIDDEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    private String address1;

    private String address2;

    private double mapX;

    private double mapY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    @Builder.Default
    public AccountStatus accountStatus = AccountStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_uuid")
    private Owner owner;

    //Mate는 하나만 등록가능하므로 OneToOne으로 변경하고 테스트하기
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mate> mate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dog> dog;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> board;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chatting> chatting;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }
}
