package pupket.togedogserver.domain.user.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.MateActiveResponse;
import pupket.togedogserver.domain.user.entity.User;

@Repository
public interface CustomMateRepository {
    Page<FindMateResponse> MateList(Pageable pageable, Long uuid);

    Page<BoardFindResponse> findMyScheduleList(Long mateId, Pageable pageable);

    MateActiveResponse findMateActions(Long mateUuid, User findUser);

}
