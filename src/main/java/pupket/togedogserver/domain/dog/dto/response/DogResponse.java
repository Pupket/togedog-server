package pupket.togedogserver.domain.dog.dto.response;

import lombok.Data;
import pupket.togedogserver.domain.dog.constant.DogType;

import java.util.Set;

@Data
public class DogResponse {

    private Long dogId;
    private String name;
    private String dogType;
    private boolean dogGender;
    private boolean neutered;
    private Long weight;
    private String notes;
    private String dogImage;
    private Set<String> dogPersonalityTags;
    private boolean vaccine;
    private int age;



}
