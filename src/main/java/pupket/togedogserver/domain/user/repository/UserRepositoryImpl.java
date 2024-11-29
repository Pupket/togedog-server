package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.jpaRepository.UserJPARepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJPARepository userRepository;


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    @Override
    public Optional<User> findByUuid(Long memberUuid) {
        return userRepository.findByUuid(memberUuid);
    }

    @Override
    public void updateFcmTokenByUuid(String fcmToken, Long uuid) {
        userRepository.updateFcmTokenByUuid(fcmToken, uuid);
    }

    @Override
    public int updateFcmTokenToNullByUuid(Long uuid) {
        return userRepository.updateFcmTokenToNullByUuid(uuid);
    }

    @Override
    public List<String> findAllNickname() {
        return userRepository.findAllNickname();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
