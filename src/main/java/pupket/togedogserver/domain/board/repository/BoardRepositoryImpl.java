package pupket.togedogserver.domain.board.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardJPARepository;
import pupket.togedogserver.domain.board.service.port.BoardRepository;
import pupket.togedogserver.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardJPARepository boardJPARepository;

    @Override
    public Optional<Board> findByUserAndBoardId(User findUser, Long id) {
        return boardJPARepository.findByUserAndBoardId(findUser, id);
    }

    @Override
    public Optional<Board> findByBoardId(Long boardId) {
        return  boardJPARepository.findByBoardId(boardId);
    }

    @Override
    public Optional<List<Board>> findByUser(User findUser) {
        return boardJPARepository.findByUser(findUser);
    }

    @Override
    public Board save(Board board) {
        return boardJPARepository.save(board);
    }

    @Override
    public void delete(Board findBoard) {
        boardJPARepository.delete(findBoard);
    }

    @Override
    public Optional<Board> findById(Long boardId) {
        return boardJPARepository.findById(boardId);
    }

    @Override
    public List<Board> findConflictOwnerMatches(List<Long> dogIdList, LocalDateTime startTime, LocalDateTime endTime, LocalDate pickUpDay, Long boardId) {
        log.info("startTime={} endTime={}", startTime, endTime);
        return boardJPARepository.findConflictOwnerMatches(dogIdList,startTime,endTime,pickUpDay,boardId);
    }
}
