package pupket.togedogserver.domain.match.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MatchJPARepository extends JpaRepository<Match, Long> {


    List<Match> findByOwner(Owner owner);

    Optional<Match> findByBoardAndMate(Board findBoard, Mate findMate);

    @Query("SELECT m from matching  m join m.board b" +
            " WHERE m.mate.mateUuid = :mateUuid" +
            " AND m.completeStatus = 'INCOMPLETE' " +
            "AND b.startTime < :startTime " +
            "AND b.endTime > :endTime " +
            "AND b.pickUpDay = :pickupDay")
    List<Match> findConflictMatches(Long mateUuid, LocalTime startTime, LocalTime endTime, LocalDate pickupDay);
}
