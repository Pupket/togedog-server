package pupket.togedogserver.domain.dog.dto.request;

import lombok.Data;
import pupket.togedogserver.domain.user.constant.Region;
import java.util.List;

@Data
public class DogUpdateRequest {
    private Long id;
    private String name;
    private String breed;
    private boolean isNeuterized;
    private boolean gender;
    private int weight;
    private Region region;
    private String note;
    private List<String> tag;
}
