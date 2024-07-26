package pupket.togedogserver.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.User;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByUserAndBoardId(User findUser, Long id);
}
