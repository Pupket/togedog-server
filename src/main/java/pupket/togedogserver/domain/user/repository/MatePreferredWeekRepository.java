package pupket.togedogserver.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredWeek;

public interface MatePreferredWeekRepository extends JpaRepository<MatePreferredWeek, Long> {
}
