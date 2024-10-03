package pupket.togedogserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.board.repository.CustomBoardRepositoryImpl;
import pupket.togedogserver.domain.match.constant.MatchStatus;
import pupket.togedogserver.domain.user.dto.response.FindMatchedScheduleResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OwnerService {

    private final CustomBoardRepositoryImpl customBoardRepositoryImpl;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    //Owner가 내 산책 일정 리스트 반환
    public Page<BoardFindResponse> findMyBoards(CustomUserDetail userDetail, Pageable page) {

        return customBoardRepositoryImpl.findMyBoardList(userDetail.getUuid(), page);
    }

    public PageImpl<FindMatchedScheduleResponse> findMySchedule(CustomUserDetail userDetail, Pageable pageable) {

        User findUser = userRepository.findByUuid(userDetail.getUuid()).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );

        List<Board> findBoards = boardRepository.findByUser(findUser).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        List<FindMatchedScheduleResponse> matchedBoardResponses = getFindMatchedScheduleResponses(findBoards);

        // 매핑된 결과를 페이징하여 반환합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchedBoardResponses.size());

        return new PageImpl<>(matchedBoardResponses.subList(start, end), pageable, matchedBoardResponses.size());
    }

    private static List<FindMatchedScheduleResponse> getFindMatchedScheduleResponses(List<Board> findBoards) {
        // 요일
        // 시간
        // 가격
        // Mate 사진 URL
        return findBoards.stream()
                .filter(board -> board.getMatched().equals(MatchStatus.MATCHED))
                .map(board -> {
                    Mate mate = board.getMatch().getMate();
                    return FindMatchedScheduleResponse.builder()
                            .boardId(board.getBoardId())
                            .pickUpDay(board.getPickUpDay().toString()) // 요일
                            .startTime(board.getStartTime().toString()) // 시간
                            .endTime(board.getEndTime().toString())
                            .fee(board.getFee().toString()) // 가격
                            .feeType(board.getFeeType().toString())
                            .mateNickname(mate.getUser().getNickname())
                            .matePhotoUrl(mate.getUser().getProfileImage()) // Mate 사진 URL
                            .mateId(mate.getMateUuid())
                            .matchStatus(board.getMatch().getMatched().getStatus())
                            .completeStatus(board.getMatch().getCompleteStatus().getStatus())
                            .build();
                })
                .toList();
    }
}
