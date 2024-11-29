package pupket.togedogserver.domain.board.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.repository.jpaRepositry.WalkingPlaceTagJPARepository;
import pupket.togedogserver.domain.board.service.port.WalkingPlaceTagRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class WalkingPlaceTagRepositoryImpl implements WalkingPlaceTagRepository {
    private final WalkingPlaceTagJPARepository walkingPlaceTagJPARepository;


    @Override
    public Optional<List<WalkingPlaceTag>> findAllByBoard(Board findBoard) {
        return walkingPlaceTagJPARepository.findAllByBoard(findBoard);
    }

    @Override
    public WalkingPlaceTag save(WalkingPlaceTag walkingPlaceTag) {
        return walkingPlaceTagJPARepository.save(walkingPlaceTag);
    }

    @Override
    public void deleteAll(List<WalkingPlaceTag> existingTags) {
        walkingPlaceTagJPARepository.deleteAll(existingTags);
    }

    @Override
    public void saveAll(Set<WalkingPlaceTag> newTags) {
        walkingPlaceTagJPARepository.saveAll(newTags);
    }
}
