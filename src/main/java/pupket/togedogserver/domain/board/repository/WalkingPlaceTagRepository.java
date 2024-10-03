package pupket.togedogserver.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;

import java.util.List;
import java.util.Optional;

public interface WalkingPlaceTagRepository extends JpaRepository<WalkingPlaceTag, Long> {
    Optional<List<WalkingPlaceTag>> findAllByBoard(Board findBoard);
}
