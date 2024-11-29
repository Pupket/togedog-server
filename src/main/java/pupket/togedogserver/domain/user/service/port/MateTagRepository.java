package pupket.togedogserver.domain.user.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MateTagRepository {

    Optional<MateTag> findByMate(Mate findMate);

    Optional<List<MateTag>> findAllByMate(Mate findMate);

    void deleteAllByMate(Mate findMate);

    MateTag save(MateTag mateTag);

    List<MateTag> saveAll(Set<MateTag> mateTags);

    void deleteAll(List<MateTag> findMateTag);
}
