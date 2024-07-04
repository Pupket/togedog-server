package pupket.togedogserver.domain.token.entity;

import jakarta.persistence.*;
import lombok.*;
import pupket.togedogserver.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class SocialAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String socialAccessToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User user;


    public static SocialAccessToken of(String socialAccessToken, User user) {
        return SocialAccessToken.builder()
                .socialAccessToken(socialAccessToken)
                .user(user)
                .build();
    }


    public void updateSocialAccessToken(String socialAccessToken) {
        this.socialAccessToken = socialAccessToken;
    }
}
