package pupket.togedogserver.domain.board.repository.jpaRepositry;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardJPARepository extends JpaRepository<Board, Long> {
    Optional<Board> findByUserAndBoardId(User findUser, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Board> findByBoardId(Long boardId);

    @Query("SELECT b FROM Board b " +
            "JOIN b.user u " +
            "JOIN u.owner o " +
            "JOIN o.match m " +
            "WHERE m.matched = 'MATCHED' AND u = :findUser")
    @EntityGraph(attributePaths = {"match.mate.user"})
    Optional<List<Board>> findByUser(User findUser);

    @Query("SELECT DISTINCT b FROM Board b " +
            "JOIN b.boardDog bd " +
            "JOIN b.match m " +
            "WHERE bd.dog.dogId IN :dogIdList " +
            "AND (m.completeStatus = 'INCOMPLETE' AND m.matched != 'REJECTED') " +
            "AND b.pickUpDay = :pickUpDay " +
            "AND ((b.startTime <= :endTime AND b.endTime >= :startTime)) " +
            "AND b.deleted = false " +
            "AND b.boardId != :boardId")
    List<Board> findConflictOwnerMatches(@Param("dogIdList") List<Long> dogIdList,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("pickUpDay") LocalDate pickUpDay,
                                         @Param("boardId") Long boardId);
}