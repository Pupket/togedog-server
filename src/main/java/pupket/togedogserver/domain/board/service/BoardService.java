package pupket.togedogserver.domain.board.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

public interface BoardService {

    void create(CustomUserDetail userDetail, BoardCreateRequest boardCreateRequest);

    BoardFindResponse find(CustomUserDetail userDetail, Long boardId);

    void update(CustomUserDetail userDetail, BoardUpdateRequest request);

    void delete(CustomUserDetail userDetail, Long id);

    Page<BoardFindResponse> findRandom(Pageable pageable);
}
