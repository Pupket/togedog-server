package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FindMateResponse {

    private String nickname;
    private String gender;
    private int age;
    PreferredDetailsResponse preferred;
    private String region;
    private String profileImage;
    private int accommodatableDogsCount;
    private String career;
}
