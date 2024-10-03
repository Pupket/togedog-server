package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DogActiveResponse {
    private String name;
    private String walkTime;
    private Long walkCount;
}
