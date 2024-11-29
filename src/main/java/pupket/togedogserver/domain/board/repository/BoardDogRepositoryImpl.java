package pupket.togedogserver.domain.board.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.repository.jpaRepositry.BoardDogJPARepository;
import pupket.togedogserver.domain.board.service.port.BoardDogRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardDogRepositoryImpl implements BoardDogRepository {
    private final BoardDogJPARepository boardDogJPARepository;

    @Override
    public void deleteAllByBoard(Board findBoard) {
        boardDogJPARepository.deleteAllByBoard(findBoard);
    }

    @Override
    public BoardDog save(BoardDog boardDog) {
        return boardDogJPARepository.save(boardDog);
    }

    @Override
    public List<BoardDog> saveAll(List<BoardDog> boardDogList) {
        return boardDogJPARepository.saveAll(boardDogList);
    }
}
