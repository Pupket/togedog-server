package pupket.togedogserver.domain.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;

public interface CustomBoardRepository {
    Page<BoardFindResponse> BoardList(Pageable pageable);

}
