package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindMateAndDogResponse {

    DogActiveResponse dogActiveResponse;
    MateActiveResponse mateActiveResponse;
}
