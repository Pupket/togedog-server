package pupket.togedogserver.domain.dog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class DogResponse {

    private Long dogId;
    private String name;
    private String dogType;
    private String breed;
    private boolean dogGender;
    private boolean neutered;
    private Long weight;
    private String notes;
    private String dogImage;
    private Set<String> dogPersonalityTags;
    private boolean vaccine;
    private int age;



}
