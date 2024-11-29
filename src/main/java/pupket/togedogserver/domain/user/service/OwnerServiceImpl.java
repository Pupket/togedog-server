package pupket.togedogserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.board.repository.CustomBoardRepositoryImpl;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.controller.port.OwnerService;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerServiceImpl implements OwnerService {

    private final CustomBoardRepositoryImpl customBoardRepositoryImpl;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    private static List<FindMatchedScheduleResponse> getFindMatchedScheduleResponses(List<Board> findBoards) {
        // 요일
        // 시간
        // 가격
        // Mate 사진 URL
        return findBoards.stream()
                .filter(board -> board.getMatched().equals(MatchStatus.MATCHED))
                .map(board -> {
                    Mate mate = board.getMatch().getMate();
                    return FindMatchedScheduleResponse.from(board, mate);
                })
                .toList();
    }

    //Owner가 내 산책 일정 리스트 반환
    @Override
    public Page<BoardFindResponse> findMyBoards(CustomUserDetail userDetail, Pageable page) {
        log.info("Finding boards for user ID: {}", userDetail.getUuid());
        return customBoardRepositoryImpl.findMyBoardList(userDetail.getUuid(), page);
    }

    @Override
    public PageImpl<FindMatchedScheduleResponse> findMySchedule(CustomUserDetail userDetail, Pageable pageable) {
        log.info("Finding schedule for user ID: {}", userDetail.getUuid());
        User findUser = getUserByUuid(userDetail.getUuid());
        List<Board> findBoards = getBoardsByUser(findUser);

        List<FindMatchedScheduleResponse> matchedBoardResponses = getFindMatchedScheduleResponses(findBoards);

        // 매핑된 결과를 페이징하여 반환합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchedBoardResponses.size());

        log.info("Returning paginated schedule for user ID: {}", userDetail.getUuid());
        return new PageImpl<>(matchedBoardResponses.subList(start, end), pageable, matchedBoardResponses.size());
    }

    private User getUserByUuid(Long uuid) {
        log.debug("Fetching user by UUID: {}", uuid);
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    private List<Board> getBoardsByUser(User user) {
        log.debug("Fetching boards for user ID: {}", user.getUuid());
        return boardRepository.findByUser(user).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
    }
}
