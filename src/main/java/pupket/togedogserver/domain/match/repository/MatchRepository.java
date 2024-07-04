package pupket.togedogserver.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.match.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
