package pupket.togedogserver.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {


    Optional<List<Match>> findByOwner(Owner owner);

    Optional<Match> findByBoardAndMate(Board findBoard, Mate findMate);
}
