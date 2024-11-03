package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindUserInfoResponse {

    private Long uuid;
    private String email;
    private String name;
    private String platform;
    private String phoneNumber;
}
