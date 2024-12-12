package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.notification.entity.Notification;
import pupket.togedogserver.domain.user.constant.AccountStatus;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET account_status = 'DELETED' WHERE uuid = ?")
@SQLRestriction("account_status = 'ACTIVE'")
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

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    @JoinColumn(name = "owner_uuid")
    private Owner owner;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    private Mate mate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Dog> dog;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Board> board;

    @OneToMany(mappedBy = "user",cascade = CascadeType.PERSIST)
    private List<Notification> notification ;

    private String fcmToken;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    @PreRemove
    public void onPreRemove() {
        deleteOwner();
        deleteMate();
        deleteDogs();
        deleteBoards();
    }

    private void deleteOwner() {
        if (owner != null) {
            owner.setIsDeleted(true);
        }
    }

    private void deleteMate() {
        if (mate != null) {
            mate.setDeleted(true);
        }
    }

    private void deleteDogs() {
        if (dog != null && !dog.isEmpty()) {
            dog.forEach(d -> d.setDeleted(true));
        }
    }

    private void deleteBoards() {
        if (board != null && !board.isEmpty()) {
            board.forEach(b -> b.setDeleted(true));
        }
    }

}
