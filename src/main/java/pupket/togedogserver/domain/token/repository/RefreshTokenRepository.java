package pupket.togedogserver.domain.token.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.token.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findByMemberId(Long memberId);

    boolean existsByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refreshToken);
}
