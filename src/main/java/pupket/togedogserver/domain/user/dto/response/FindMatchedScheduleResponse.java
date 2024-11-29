package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.user.entity.mate.Mate;

@Data
@Builder
public class FindMatchedScheduleResponse {

    private Long boardId;
    private String pickUpDay;
    private String startTime;
    private String endTime;
    private String fee;
    private String mateNickname;
    private String matePhotoUrl;
    private String feeType;
    private Long mateId;
    private String matchStatus;
    private String completeStatus;


    public static FindMatchedScheduleResponse from(Board board, Mate mate) {
        return  FindMatchedScheduleResponse.builder()
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
    }
}
