package pupket.togedogserver.domain.user.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUuid(Long memberUuid);

    @Modifying
    @Query("update users u set u.fcmToken = :fcmToken where u.uuid = :uuid")
    void updateFcmTokenByUuid(String fcmToken, Long uuid);

    @Modifying
    @Query("update users u set u.fcmToken = null where u.uuid = :uuid")
    int updateFcmTokenToNullByUuid(Long uuid);

    @Query("select u.nickname from users u")
    List<String> findAllByNickname();
}
