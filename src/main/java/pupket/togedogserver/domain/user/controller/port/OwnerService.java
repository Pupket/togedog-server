package pupket.togedogserver.domain.user.controller.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

@Service
public interface OwnerService {
    Page<BoardFindResponse> findMyBoards(CustomUserDetail userDetail, Pageable page);

    PageImpl<FindMatchedScheduleResponse> findMySchedule(CustomUserDetail userDetail, Pageable pageable);
}
