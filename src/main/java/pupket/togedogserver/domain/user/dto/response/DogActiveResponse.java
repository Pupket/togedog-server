package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DogActiveResponse {
    private String name;
    private Long walkTime;
    private Long walkCount;
}
