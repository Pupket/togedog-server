package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredTime;
import pupket.togedogserver.domain.user.repository.jpaRepository.MatePreferredTimeJPARepository;
import pupket.togedogserver.domain.user.service.port.MatePreferredTimeRepository;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class MatePreferredTimeRepositoryImpl implements MatePreferredTimeRepository {
    private final MatePreferredTimeJPARepository matePreferredTimeJPARepository;

    @Override
    public void deleteAllByMate(Mate findMate) {
        matePreferredTimeJPARepository.deleteAllByMate(findMate);
    }

    @Override
    public MatePreferredTime save(MatePreferredTime matePreferredTime) {
        return matePreferredTimeJPARepository.save(matePreferredTime);
    }

    @Override
    public List<MatePreferredTime> saveAll(Set<MatePreferredTime> preferredTimes) {
        return matePreferredTimeJPARepository.saveAll(preferredTimes);
    }
}
