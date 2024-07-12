package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class FindMateResponse {

    private String nickname;
    private String gender;
    private int age;
    private Set<String> preferredWeek;
    private Set<String> preferredTime;
    private Set<String> preferredStyle;
    private Set<String> preferredBreed;
    private String region;
    private String profileImage;
    private int accommodatableDogsCount;
}
