package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MateActiveResponse {
    private String mateName;
    private Long walkTime;
    private Long walkCount;
}
