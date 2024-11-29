package pupket.togedogserver.domain.user.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUuid(Long memberUuid);

    void updateFcmTokenByUuid(String fcmToken, Long uuid);

    int updateFcmTokenToNullByUuid(Long uuid);

    List<String> findAllNicknames();

    User save(User user);
}
