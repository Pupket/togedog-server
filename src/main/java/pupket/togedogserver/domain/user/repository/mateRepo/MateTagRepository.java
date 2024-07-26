package pupket.togedogserver.domain.user.repository.mateRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;

import java.util.List;
import java.util.Optional;

public interface MateTagRepository extends JpaRepository<MateTag, Long> {
    Optional<MateTag> findByMate(Mate findMate);

    Optional<List<MateTag>> findAllByMate(Mate findMate);

    void deleteAllByMate(Mate findMate);
}
