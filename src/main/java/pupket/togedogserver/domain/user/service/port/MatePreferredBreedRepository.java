package pupket.togedogserver.domain.user.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MatePreferredBreed;

import java.util.List;
import java.util.Set;

@Repository
public interface MatePreferredBreedRepository {
    void deleteAllByMate(Mate findMate);

    MatePreferredBreed save(MatePreferredBreed breed);

    List<MatePreferredBreed> saveAll(Set<MatePreferredBreed> preferredBreeds);
}
