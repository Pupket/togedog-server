package pupket.togedogserver.domain.board.repository.jpaRepositry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;

@Repository
public interface BoardDogJPARepository extends JpaRepository<BoardDog, Long> {

    void deleteAllByBoard(Board findBoard);

}
