package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindMateAndDogResponse {

    DogActiveResponse dogActiveResponse;
    MateActiveResponse mateActiveResponse;

    public static FindMateAndDogResponse from(MateActiveResponse mateActions, DogActiveResponse dogActions) {
        return FindMateAndDogResponse.builder()
                .mateActiveResponse(mateActions)
                .dogActiveResponse(dogActions)
                .build();
    }
}
