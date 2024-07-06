package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredTime;

public interface MatePreferredTimeRepository extends JpaRepository<MatePreferredTime, Long> {
}
