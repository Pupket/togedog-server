package pupket.togedogserver.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.match.constant.CompleteStatus;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
                        .collect(Collectors.toList()))
                .feeType(EnumMapper.enumToKorean(findBoard.getFeeType()))
                .dogs(boardDogRespons) // 여러 마리의 개 정보 추가
                .completeStatus(findBoard.getMatch()==null? CompleteStatus.INCOMPLETE.getStatus() : findBoard.getMatch().getCompleteStatus().getStatus())
                .build();
    }
}
