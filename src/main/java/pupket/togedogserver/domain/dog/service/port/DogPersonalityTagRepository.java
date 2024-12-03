package pupket.togedogserver.domain.dog.service.port;

import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;

import java.util.List;
import java.util.Set;

public interface DogPersonalityTagRepository {
    void deleteByDog(Dog findDog);

    void deleteAllByDog(Dog savedDog);

    DogPersonalityTag save(DogPersonalityTag personalityTag);

    List<DogPersonalityTag> saveAll(Set<DogPersonalityTag> tags);

    void deleteAll(Set<DogPersonalityTag> tags);
}
