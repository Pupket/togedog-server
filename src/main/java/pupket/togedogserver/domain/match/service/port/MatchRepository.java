package pupket.togedogserver.domain.match.service.port;


import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    List<Match> findByOwner(Owner owner);

    Optional<Match> findByBoardAndMate(Board findBoard, Mate findMate);

    Match save(Match match);

    Optional<Match> findById(Long matchId);
}
