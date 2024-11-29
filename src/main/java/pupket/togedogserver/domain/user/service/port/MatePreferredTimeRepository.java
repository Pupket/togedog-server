package pupket.togedogserver.domain.user.service.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredTime;

import java.util.List;
import java.util.Set;

@Repository
public interface MatePreferredTimeRepository {
    void deleteAllByMate(Mate findMate);

    MatePreferredTime save(MatePreferredTime matePreferredTime);

    List<MatePreferredTime> saveAll(Set<MatePreferredTime> preferredTimes);
}
