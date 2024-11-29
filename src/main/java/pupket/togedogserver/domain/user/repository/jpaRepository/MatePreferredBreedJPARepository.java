package pupket.togedogserver.domain.user.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredBreed;

public interface MatePreferredBreedJPARepository extends JpaRepository<MatePreferredBreed, Long> {
    void deleteAllByMate(Mate findMate);

}

