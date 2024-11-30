package pupket.togedogserver.domain.dog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import pupket.togedogserver.domain.dog.repository.jpaRepository.DogPersonalityTagJPARepository;
import pupket.togedogserver.domain.dog.service.port.DogPersonalityTagRepository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DogPersonalityTagRepositoryImpl implements DogPersonalityTagRepository {
    private final DogPersonalityTagJPARepository dogPersonalityTagJPARepository;

    @Override
    public void deleteByDog(Dog findDog) {
        dogPersonalityTagJPARepository.deleteByDog(findDog);
    }

    @Override
    public void deleteAllByDog(Dog savedDog) {
        dogPersonalityTagJPARepository.deleteAllByDog(savedDog);
    }

    @Override
    public DogPersonalityTag save(DogPersonalityTag personalityTag) {
        return dogPersonalityTagJPARepository.save(personalityTag);
    }

    @Override
    public void saveAll(Set<DogPersonalityTag> tags) {
        dogPersonalityTagJPARepository.saveAll(tags);
    }
}
