package pupket.togedogserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.chat.entity.Chatting;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.user.constant.AccountStatus;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Visibility;
import pupket.togedogserver.domain.user.dto.request.SignUpRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private LocalDate birthDay;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'HIDDEN'")
    @Column(nullable = false)
    @Builder.Default
    private Visibility genderVisibility = Visibility.HIDDEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    /**
     * 도로명 주소
     */
    private String address1;

    /**
     * 세부 주소
     */
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

    @OneToMany(mappedBy = "user")
    private List<Mate> mate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dog> dog;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> board;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chatting> chatting;

    // 권한 정보를 반환하는 메서드
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    public void updatePassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(password);
    }


    public void updateInfo(UpdateRequest updateReq) {
        this.nickname = updateReq.getNickName();
//        this.profileImage = updateReq.get();
        this.address1 = updateReq.getAddress1();
        this.address2 = updateReq.getAddress2();
        this.genderVisibility = Visibility.valueOf(updateReq.getGenderVisibility());
    }

    public void updateInfo(SignUpRequest signUpRequest) {
        this.nickname = signUpRequest.getNickname();
//        this.profileImage = updateReq.get();
        this.genderVisibility = Visibility.valueOf(signUpRequest.getGenderVisibility());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.birthDay = LocalDate.parse(signUpRequest.getBirthDay(), formatter);
        ;
    }

    public void updateProfile(String name, String picture) {
        this.nickname = name;
        this.profileImage = picture;
    }

    public void updateStatus() {
        this.accountStatus = AccountStatus.DELETED;
    }
}
