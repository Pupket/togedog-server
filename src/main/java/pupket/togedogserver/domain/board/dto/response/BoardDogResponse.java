package pupket.togedogserver.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class BoardDogResponse {
    private String name;
    private int age;
    private String breed;
    private String dogType;
    private String dogGender;
    private String dogProfileImage;
}