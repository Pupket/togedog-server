package pupket.togedogserver.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
}
