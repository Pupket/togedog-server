package pupket.togedogserver.domain.user.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredWeek;

import java.util.List;
import java.util.Set;

@Repository
public interface MatePreferredWeekRepository  {
    void deleteAllByMate(Mate findMate);

    MatePreferredWeek save(MatePreferredWeek matePreferredWeek);

    List<MatePreferredWeek> saveAll(Set<MatePreferredWeek> preferredWeeks);
}
