package pupket.togedogserver.domain.board.service.port;

import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;

import java.util.List;

public interface BoardDogRepository {
    void deleteAllByBoard(Board findBoard);

    BoardDog save(BoardDog boardDog);

    List<BoardDog> saveAll(List<BoardDog> boardDogList);
}
