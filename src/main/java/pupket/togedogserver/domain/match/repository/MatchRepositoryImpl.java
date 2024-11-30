package pupket.togedogserver.domain.match.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.match.repository.jpaRepository.MatchJPARepository;
import pupket.togedogserver.domain.match.service.port.MatchRepository;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepository {
    private final MatchJPARepository matchJPARepository;

    @Override
    public List<Match> findByOwner(Owner owner) {
        return matchJPARepository.findByOwner(owner);
    }

    @Override
    public Optional<Match> findByBoardAndMate(Board findBoard, Mate findMate) {
        return matchJPARepository.findByBoardAndMate(findBoard, findMate);
    }

    @Override
    public Match save(Match match) {
        return matchJPARepository.save(match);
    }

    @Override
    public Optional<Match> findById(Long matchId) {
        return matchJPARepository.findById(matchId);
    }
}
