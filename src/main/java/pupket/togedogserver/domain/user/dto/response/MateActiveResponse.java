package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MateActiveResponse {
    private String mateName;
    private String walkTime;
    private Long walkCount;
}
