package pupket.togedogserver.domain.board.repository.jpaRepositry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalkingPlaceTagJPARepository extends JpaRepository<WalkingPlaceTag, Long> {
    Optional<List<WalkingPlaceTag>> findAllByBoard(Board findBoard);

}
