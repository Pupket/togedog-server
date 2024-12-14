package pupket.togedogserver.domain.match.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.domain.user.entity.Owner;
import pupket.togedogserver.domain.user.entity.mate.Mate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchJPARepository extends JpaRepository<Match, Long> {


    List<Match> findByOwner(Owner owner);

    Optional<Match> findByBoardAndMate(Board findBoard, Mate findMate);

    @Query("SELECT m FROM matching m JOIN m.board b " +
            "WHERE m.mate.mateUuid = :mateUuid " +
            "AND m.completeStatus = 'INCOMPLETE'  " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime " +
            "AND b.pickUpDay = :pickupDay " +
            "AND b.boardId != :boardId")
    List<Match> findConflictMatches(@Param("mateUuid") Long mateUuid,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("pickupDay") LocalDate pickupDay,
                                    @Param("boardId") long boardId);



}
