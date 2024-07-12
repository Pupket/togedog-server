package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.Optional;

public interface MateRepository extends JpaRepository<Mate, Long> {

    Optional<Mate> findByUser(User findUserByNickname);
}
