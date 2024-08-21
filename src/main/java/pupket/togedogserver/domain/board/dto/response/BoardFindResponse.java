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

    private String title;
    private LocalDate pickUpDay;
    private String fee;  // String으로 변경하여 한글 반환
    private String startTime;  // String으로 변경
    private String endTime;  // String으로 변경
    private String pickupLocation2;
    private List<String> walkingPlaceTag;
    private String feeType;

    private String name;
    private int age;
    private String dogType;  // String으로 변경하여 한글 반환

}
