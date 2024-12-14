package pupket.togedogserver.domain.board.service.port;

import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardRepository {
    Optional<Board> findByUserAndBoardId(User findUser, Long id);

    Optional<Board> findByBoardId(Long boardId);

    Optional<List<Board>> findByUser(User findUser);

    Board save(Board board);

    void delete(Board findBoard);

    Optional<Board> findById(Long boardId);

    List<Board> findConflictOwnerMatches(List<Long> dogIdList, LocalDateTime startTime, LocalDateTime endTime, LocalDate pickUpDay, Long boardId);

}
