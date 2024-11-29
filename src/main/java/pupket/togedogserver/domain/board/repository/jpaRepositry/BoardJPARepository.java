package pupket.togedogserver.domain.board.repository.jpaRepositry;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.User;

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
            "JOIN matching m ON m.owner.ownerUuid = o.ownerUuid " +
            "WHERE m.matched = 'MATCHED' AND u = :findUser")
    Optional<List<Board>> findByUser(User findUser);

    Optional<List<Board>> findAllByUserAndBoardId(User findUser, Long id);
}