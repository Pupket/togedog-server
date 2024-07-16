package pupket.togedogserver.domain.dog.dto.response;

import lombok.Data;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import java.util.Date;
import java.util.List;

@Data
public class DogResponse {

    private int id;
    private String name;
    private boolean dogGender;
    private boolean neutered;
    private Date birthday;
    private Long weight;
    private String notes;
    private String dogImage;
    private List<DogPersonalityTag> dogPersonalityTags;


}
