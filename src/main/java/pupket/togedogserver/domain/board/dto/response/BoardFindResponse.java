package pupket.togedogserver.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.dog.constant.Breed;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardFindResponse {

    private String title;
    private LocalDate pickUpDay;
    private Long fee;
    private LocalTime startTime;
    private LocalTime endTime;
    private String pickupLocation2;
    private List<String> walkingPlaceTag;

    private String name;
    private int age;
    private Breed breed;

}
