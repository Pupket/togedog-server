package pupket.togedogserver.domain.user.repository.mateRepo;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.Optional;

public interface MateRepository extends JpaRepository<Mate, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Mate> findByUser(User User);
}
