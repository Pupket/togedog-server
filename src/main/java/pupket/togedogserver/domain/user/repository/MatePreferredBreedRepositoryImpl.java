package pupket.togedogserver.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredBreed;
import pupket.togedogserver.domain.user.repository.jpaRepository.MatePreferredBreedJPARepository;
import pupket.togedogserver.domain.user.service.port.MatePreferredBreedRepository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MatePreferredBreedRepositoryImpl implements MatePreferredBreedRepository {
    private final MatePreferredBreedJPARepository matePreferredBreedJPARepository;

    @Override
    public void deleteAllByMate(Mate findMate) {
        matePreferredBreedJPARepository.deleteAllByMate(findMate);
    }

    @Override
    public MatePreferredBreed save(MatePreferredBreed breed) {
        return matePreferredBreedJPARepository.save(breed);
    }

    @Override
    public List<MatePreferredBreed> saveAll(Set<MatePreferredBreed> preferredBreeds) {
        return matePreferredBreedJPARepository.saveAll(preferredBreeds);
    }
}
