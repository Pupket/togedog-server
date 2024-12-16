package pupket.togedogserver.domain.board.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;

public interface CustomBoardRepository {
    Page<BoardFindResponse> findRandomBoardList(Pageable pageable);

    Page<BoardFindResponse> findMyBoardList(Long uuid, Pageable pageable);


}
