package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredWeek;
import pupket.togedogserver.domain.user.repository.jpaRepository.MatePreferredWeekJPARepository;
import pupket.togedogserver.domain.user.service.port.MatePreferredWeekRepository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MatePreferredWeekRepositoryImpl implements MatePreferredWeekRepository {
    private final MatePreferredWeekJPARepository matePreferredWeekJPARepository;

    @Override
    public void deleteAllByMate(Mate findMate) {
        matePreferredWeekJPARepository.deleteAllByMate(findMate);
    }

    @Override
    public MatePreferredWeek save(MatePreferredWeek matePreferredWeek) {
        return matePreferredWeekJPARepository.save(matePreferredWeek);
    }

    @Override
    public List<MatePreferredWeek> saveAll(Set<MatePreferredWeek> preferredWeeks) {
        return matePreferredWeekJPARepository.saveAll(preferredWeeks);
    }
}
