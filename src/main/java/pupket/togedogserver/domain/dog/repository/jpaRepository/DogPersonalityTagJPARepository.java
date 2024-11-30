package pupket.togedogserver.domain.dog.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;

public interface DogPersonalityTagJPARepository extends JpaRepository<DogPersonalityTag, Long> {
    void deleteByDog(Dog findDog);

    void deleteAllByDog(Dog savedDog);
}
