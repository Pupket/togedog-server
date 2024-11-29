package pupket.togedogserver.domain.board.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardJPARepository;
import pupket.togedogserver.domain.board.service.port.BoardRepository;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

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
    public Optional<List<Board>> findAllByUserAndBoardId(User findUser, Long id) {
        return boardJPARepository.findAllByUserAndBoardId(findUser, id);
    }

    @Override
    public Board save(Board board) {
        return boardJPARepository.save(board);
    }

    @Override
    public void delete(Board findBoard) {
        boardJPARepository.delete(findBoard);
    }
}
