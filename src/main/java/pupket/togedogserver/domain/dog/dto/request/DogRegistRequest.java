package pupket.togedogserver.domain.dog.dto.request;

import lombok.Data;
import pupket.togedogserver.domain.user.constant.Region;
import java.util.Date;
import java.util.List;

@Data
public class DogRegistRequest {
    private String name;
    private String breed;
    private boolean isNeuterized;
    private boolean dogGender;
    private int weight;
    private Region region;
    private String notes;
    private List<String> tag;
    private Date birthday;
}
