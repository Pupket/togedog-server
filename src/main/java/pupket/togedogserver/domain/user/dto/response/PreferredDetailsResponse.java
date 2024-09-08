package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class PreferredDetailsResponse {
    private Set<String> week;
    private Set<String> time;
    private Set<String> hashTag;
    private Set<String> breed;
    private String region;
}
