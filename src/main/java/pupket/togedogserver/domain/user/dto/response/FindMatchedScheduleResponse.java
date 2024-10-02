package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

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


}
