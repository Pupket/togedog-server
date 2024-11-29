package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.domain.user.repository.jpaRepository.MateTagJPARepository;
import pupket.togedogserver.domain.user.service.port.MateTagRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MateTagRepositoryImpl implements MateTagRepository {
    private final MateTagJPARepository mateTagJPARepository;

    @Override
    public Optional<MateTag> findByMate(Mate findMate) {
        return mateTagJPARepository.findByMate(findMate);
    }

    @Override
    public Optional<List<MateTag>> findAllByMate(Mate findMate) {
        return mateTagJPARepository.findAllByMate(findMate);
    }

    @Override
    public void deleteAllByMate(Mate findMate) {
        mateTagJPARepository.deleteAllByMate(findMate);
    }

    @Override
    public MateTag save(MateTag mateTag) {
        return mateTagJPARepository.save(mateTag);
    }

    @Override
    public List<MateTag> saveAll(Set<MateTag> mateTags) {
        return mateTagJPARepository.saveAll(mateTags);
    }

    @Override
    public void deleteAll(List<MateTag> findMateTag) {
        mateTagJPARepository.deleteAll(findMateTag);
    }

}
