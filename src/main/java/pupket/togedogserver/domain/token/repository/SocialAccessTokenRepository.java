package pupket.togedogserver.domain.token.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.token.entity.SocialAccessToken;
import pupket.togedogserver.domain.user.entity.User;
import java.util.Optional;

public interface SocialAccessTokenRepository extends JpaRepository<SocialAccessToken, Long> {
    Optional<SocialAccessToken> findByUser(User user);
}
