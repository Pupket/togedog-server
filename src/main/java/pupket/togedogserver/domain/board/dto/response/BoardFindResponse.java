package pupket.togedogserver.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.domain.match.entity.Match;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class BoardFindResponse {

    private Long boardId;
    private Long userId;
    private String title;
    private LocalDate pickUpDay;
    private String fee;
    private String startTime;
    private String endTime;
    private String pickupLocation1;
    private List<String> walkingPlaceTag;
    private String feeType;

    private List<BoardDogResponse> dogs;

    private String completeStatus;

    public static BoardFindResponse to(Board findBoard, List<BoardDogResponse> boardDogRespons) {

        return BoardFindResponse.builder()
                .boardId(findBoard.getBoardId())
                .userId(findBoard.getUser().getUuid())
                .title(findBoard.getTitle())
                .pickUpDay(findBoard.getPickUpDay())
                .fee(findBoard.getFee().toString())
                .startTime(findBoard.getStartTime().toString())
                .endTime(findBoard.getEndTime().toString())
                .pickupLocation1(findBoard.getPickupLocation1())
                .walkingPlaceTag(findBoard.getWalkingPlaceTag().stream()
                        .map(WalkingPlaceTag::getPlaceName)
                        .toList())
                .feeType(EnumMapper.enumToKorean(findBoard.getFeeType()))
                .dogs(boardDogRespons) // 여러 마리의 개 정보 추가
                .completeStatus(getCompleteStatus(findBoard)).build();
    }

    private static String getCompleteStatus(Board board) {
        // Null 체크 및 비어 있는 경우 처리
        if (board.getMatch() == null || board.getMatch().isEmpty()) {
            log.info("match상태는 null입니다.");
            return CompleteStatus.INCOMPLETE.getStatus();
        }

        // 하나라도 COMPLETE 상태가 아니라면 INCOMPLETE 반환
        for (Match match : board.getMatch()) {
            if (match.getCompleteStatus().equals(CompleteStatus.COMPLETE)) {
                return CompleteStatus.INCOMPLETE.getStatus();
            }
        }
        return CompleteStatus.INCOMPLETE.getStatus();
    }
}
