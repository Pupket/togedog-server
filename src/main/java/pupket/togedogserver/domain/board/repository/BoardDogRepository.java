package pupket.togedogserver.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;

public interface BoardDogRepository extends JpaRepository<BoardDog, Long> {
    void deleteAllByBoard(Board findBoard);
}
