package pupket.togedogserver.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByUserAndBoardId(User findUser, Long id);

    Optional<Board> findByBoardId(Long boardId);

    @Query("SELECT b FROM Board b WHERE b.user.uuid = (SELECT u.uuid FROM users u WHERE u.owner.ownerUuid = (SELECT m.owner.ownerUuid FROM matching m WHERE m.matched = 'MATCHED'))")
    Optional<List<Board>> findByUser(User findUser);

}