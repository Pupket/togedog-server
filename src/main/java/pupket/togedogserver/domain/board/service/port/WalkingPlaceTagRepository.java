package pupket.togedogserver.domain.board.service.port;

import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WalkingPlaceTagRepository {
    Optional<List<WalkingPlaceTag>> findAllByBoard(Board findBoard);

    WalkingPlaceTag save(WalkingPlaceTag walkingPlaceTag);

    void deleteAll(List<WalkingPlaceTag> existingTags);

    void saveAll(Set<WalkingPlaceTag> newTags);
}
