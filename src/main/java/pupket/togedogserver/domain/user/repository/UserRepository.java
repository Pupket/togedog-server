package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from users u where u.email=:email")
    Optional<User> findByEmail(String email);

    @Query("select u from users u join u.mate m where u.nickname = :nickname")
    Optional<User> findByNickname(String nickname);

    @Query("select u from users u  where u.uuid= :memberUuid")
    Optional<User> findByUuid(Long memberUuid);

    @Modifying
    @Query("update users u set u.fcmToken = :fcmToken where u.uuid = :uuid")
    void updateFcmTokenByUuid(String fcmToken, Long uuid);

    @Modifying
    @Query("update users u set u.fcmToken = null where u.uuid = :uuid")
    int updateFcmTokenToNullByUuid(Long uuid);

    @Query("select u.nickname from users u")
    List<String> findAllNickname();
}
