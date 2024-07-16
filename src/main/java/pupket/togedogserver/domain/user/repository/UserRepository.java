package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    @Query("select u from users u  where u.uuid= :memberUuid and u.accountStatus = 'ACTIVE' ")
    Optional<User> findByUuid(Long memberUuid);
}
